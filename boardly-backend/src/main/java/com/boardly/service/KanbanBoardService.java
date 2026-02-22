package com.boardly.service;

import com.boardly.common.dto.kanbanboard.*;
import com.boardly.data.mapper.KanbanMapper;
import com.boardly.data.mapper.UserMapper;
import com.boardly.data.model.nosql.*;
import com.boardly.data.repository.KanbanBoardRepository;
import com.boardly.data.repository.KanbanCardRepository;
import com.boardly.data.repository.UserRepository;
import com.boardly.exception.ResourceNotFoundException;
import com.boardly.security.model.AppUserDetails;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KanbanBoardService {
    private final KanbanBoardRepository kanbanBoardRepository;
    private final KanbanCardRepository kanbanCardRepository;
    private final KanbanMapper kanbanMapper;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    public KanbanBoardService(KanbanBoardRepository kanbanBoardRepository, KanbanCardRepository kanbanCardRepository, KanbanMapper kanbanMapper, UserRepository userRepository, UserMapper userMapper, NotificationService notificationService) {
        this.kanbanCardRepository = kanbanCardRepository;
        this.kanbanBoardRepository = kanbanBoardRepository;
        this.kanbanMapper = kanbanMapper;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
    }

    public void createBoard(UUID boardId) {
        KanbanBoard kanbanBoard = new KanbanBoard();
        kanbanBoard.setBoardId(boardId);
        kanbanBoard.setLists(new ArrayList<>());
        kanbanBoardRepository.save(kanbanBoard);
    }

    @Async
    public void deleteBoard(UUID boardId) {
        kanbanCardRepository.deleteAllByBoardId(boardId);
        kanbanBoardRepository.deleteByBoardId(boardId);
    }


    public KanbanBoardDTO getBoard(UUID boardId) {
        KanbanBoard kanbanBoard = kanbanBoardRepository.findByBoardId(boardId).orElseThrow(
                () -> new ResourceNotFoundException("Board not found"));

        KanbanBoardDTO kanbanBoardDTO = kanbanMapper.toDTO(kanbanBoard);
        List<KanbanCardDTO> kanbanCardDTOs = kanbanCardRepository.findAllByBoardId(boardId)
                .stream()
                .map(kanbanMapper::toDTO)
                .toList();

        Map<UUID, List<KanbanCardDTO>> kanbanCardDTOMap = kanbanCardDTOs.stream()
                .collect(Collectors.groupingBy(KanbanCardDTO::getListId));

        List<KanbanListDTO> kanbanListDTOs = kanbanBoard.getLists().stream()
                .map(list -> {
                    KanbanListDTO kanbanListDTO = kanbanMapper.toDTO(list);
                    List<KanbanCardDTO> cards = kanbanCardDTOMap
                            .getOrDefault(list.getId(), new ArrayList<>());
                    cards.sort(Comparator.comparingDouble(KanbanCardDTO::getPosition));
                    kanbanListDTO.setCards(cards);
                    return kanbanListDTO;
                })
                .sorted(Comparator.comparingDouble(KanbanListDTO::getPosition))
                .toList();

        kanbanBoardDTO.setLists(kanbanListDTOs);

        return kanbanBoardDTO;
    }

    // List operations

    public KanbanListDTO createList(UUID boardId, KanbanListCreationRequestDTO kanbanListCreationRequestDTO) {
        KanbanBoard kanbanBoard = kanbanBoardRepository.findByBoardId(boardId).orElseThrow(
                () -> new ResourceNotFoundException("Board not found"));
        KanbanList kanbanList = new KanbanList();
        kanbanList.setId(UUID.randomUUID());
        kanbanList.setTitle(kanbanListCreationRequestDTO.getTitle());
        kanbanList.setPosition(kanbanListCreationRequestDTO.getPosition());
        kanbanBoard.getLists().add(kanbanList);
        kanbanBoardRepository.save(kanbanBoard);

        boolean collision = isPositionCollision(kanbanBoard.getLists(), kanbanList.getPosition());
        if (collision || kanbanList.getPosition() <= 0.125) {
            rebalancePositions(kanbanBoard.getLists());
            kanbanBoardRepository.save(kanbanBoard);
            KanbanList list = kanbanBoard.getLists().stream()
                    .filter(l -> l.getId().equals(kanbanList.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("List not found after rebalancing"));
            kanbanList.setPosition(list.getPosition());
        }

        KanbanListDTO kanbanListDTO = kanbanMapper.toDTO(kanbanList);
        kanbanListDTO.setCards(new ArrayList<>());
        notificationService.sendToTopic("/topic/kanban/" + boardId, kanbanListDTO);
        return kanbanListDTO;
    }

    public KanbanListDTO updateList(UUID boardId, UUID listId, KanbanListUpdateRequestDTO kanbanListUpdateRequestDTO) {
        KanbanBoard board = kanbanBoardRepository.findByBoardId(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        KanbanList list = board.getLists().stream()
                .filter(l -> l.getId().equals(listId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("List not found"));

        if (kanbanListUpdateRequestDTO.getTitle() == null && kanbanListUpdateRequestDTO.getPosition() == null) {
            throw new IllegalArgumentException("At least one field (title or position) must be provided for update");
        }

        if (kanbanListUpdateRequestDTO.getTitle() != null) {
            String newTitle = kanbanListUpdateRequestDTO.getTitle().trim();
            if (newTitle.isEmpty()) {
                throw new IllegalArgumentException("Title is required");
            }
            list.setTitle(newTitle);
        }

        if (kanbanListUpdateRequestDTO.getPosition() != null) {
            moveList(board, list, kanbanListUpdateRequestDTO.getPosition());
        }

        kanbanBoardRepository.save(board);

        KanbanListDTO kanbanListDTO = kanbanMapper.toDTO(list);
        notificationService.sendToTopic("/topic/kanban/" + boardId, kanbanListDTO);
        return kanbanListDTO;
    }

    private void moveList(KanbanBoard board, KanbanList listToMove, double targetPosition) {
        if (targetPosition < 0) {
            throw new IllegalArgumentException("Position must be greater than 0");
        }

        if (targetPosition > Long.MAX_VALUE) {
            throw new IllegalArgumentException("Position must be less than " + Long.MAX_VALUE);
        }

        boolean collision = isPositionCollision(board.getLists(), targetPosition);
        if (collision || targetPosition < 0.125) {
            rebalancePositions(board.getLists());
            KanbanList list = board.getLists().stream()
                    .filter(l -> l.getId().equals(listToMove.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("List not found after rebalancing"));
            targetPosition = list.getPosition();
        }

        listToMove.setPosition(targetPosition);
    }

    private boolean isPositionCollision(List<KanbanList> lists, double targetPosition) {
        double minDistance = 0.0001;
        return lists.stream()
                .anyMatch(list -> Math.abs(list.getPosition() - targetPosition) < minDistance);
    }

    private void rebalancePositions(List<KanbanList> lists) {
        double currentPosition = Math.pow(2, 47);
        double incrementSize = Math.pow(2, 14);
        lists.sort(Comparator.comparingDouble(KanbanList::getPosition));
        for (KanbanList l : lists) {
            l.setPosition(currentPosition);
            currentPosition += incrementSize;
        }
    }

    public void deleteList(UUID boardId, UUID listId) {
        KanbanBoard kanbanBoard = kanbanBoardRepository.findByBoardId(boardId).orElseThrow(
                () -> new ResourceNotFoundException("Board not found"));
        boolean removed = kanbanBoard.getLists().removeIf(list -> list.getId().equals(listId));
        if (!removed) {
            throw new ResourceNotFoundException("List not found");
        }
        kanbanCardRepository.deleteAllByBoardIdAndListId(boardId, listId);
        kanbanBoardRepository.save(kanbanBoard);
        notificationService.sendToTopic("/topic/kanban/" + boardId, Map.of("listId", listId, "deleted", true));
    }

    // Card operations


    public KanbanCardDTO createCard(UUID boardId, KanbanCardCreationRequestDTO kanbanCardCreationRequestDTO) {
        KanbanBoard kanbanBoard = kanbanBoardRepository.findByBoardId(boardId).orElseThrow(
                () -> new ResourceNotFoundException("Board not found"));

        UUID listId = kanbanCardCreationRequestDTO.getListId();
        kanbanBoard.getLists().stream()
                .filter(list -> list.getId().equals(listId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("List not found"));

        KanbanCard kanbanCard = new KanbanCard();
        kanbanCard.setId(UUID.randomUUID());
        kanbanCard.setBoardId(boardId);
        kanbanCard.setListId(listId);
        kanbanCard.setTitle(kanbanCardCreationRequestDTO.getTitle());
        kanbanCard.setPosition(kanbanCardCreationRequestDTO.getPosition());
        kanbanCardRepository.save(kanbanCard);

        KanbanCardDTO kanbanCardDTO = kanbanMapper.toDTO(kanbanCard);
        notificationService.sendToTopic("/topic/kanban/" + boardId, kanbanCardDTO);
        return kanbanCardDTO;
    }

    public KanbanCardDetailsDTO getCard(UUID boardId, UUID cardId) {
        KanbanCard kanbanCard = kanbanCardRepository.findByBoardIdAndId(boardId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        KanbanCardDetailsDTO kanbanCardDetailsDTO = kanbanMapper.toDetailsDTO(kanbanCard);
        List<CardCommentDTO> comments = kanbanCard.getComments().stream()
                .map(comment -> {
                    CardCommentDTO cardCommentDTO = kanbanMapper.toDTO(comment);
                    userRepository.findById(comment.getAuthorId()).ifPresent(user -> cardCommentDTO.setAuthor(userMapper.toDTO(user)));
                    return cardCommentDTO;
                })
                .collect(Collectors.toList());
        kanbanCardDetailsDTO.setComments(comments);
        return kanbanCardDetailsDTO;
    }

    public void updateCard(UUID boardId, UUID cardId, KanbanCardUpdateRequestDTO updateRequest) {
        KanbanCard card = kanbanCardRepository.findByBoardIdAndId(boardId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        if (updateRequest.getTitle() != null) {
            card.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getDescription() != null) {
            card.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getPosition() != null) {
            card.setPosition(updateRequest.getPosition());
        }
        if (updateRequest.getListId() != null) {
            kanbanBoardRepository.findByBoardId(boardId)
                    .orElseThrow(() -> new ResourceNotFoundException("Board not found"))
                    .getLists().stream()
                    .filter(list -> list.getId().equals(updateRequest.getListId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("List not found"));
            card.setListId(updateRequest.getListId());
        }
        if (updateRequest.getStartDate() != null) {
            card.setStartDate(updateRequest.getStartDate());
        }
        if (updateRequest.getDueDate() != null) {
            card.setDueDate(updateRequest.getDueDate());
        }
        if (updateRequest.getLabels() != null) {
            card.setLabels(updateRequest.getLabels());
        }
        if (updateRequest.getAssignedMembers() != null) {
            card.setAssignedMembers(new HashSet<>(updateRequest.getAssignedMembers()));
        }

        kanbanCardRepository.save(card);
        notificationService.sendToTopic("/topic/kanban/" + boardId, kanbanMapper.toDTO(card));
    }

    public ChecklistDTO addChecklist(UUID boardId, UUID cardId, ChecklistDTO checklistDTO) {
        KanbanCard card = kanbanCardRepository.findByBoardIdAndId(boardId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        Checklist checklist = new Checklist();
        checklist.setId(UUID.randomUUID());
        checklist.setTitle(checklistDTO.getTitle());
        checklist.setItems(new ArrayList<>());
        card.getChecklists().add(checklist);
        kanbanCardRepository.save(card);
        ChecklistDTO newChecklistDTO = kanbanMapper.toDTO(checklist);
        notificationService.sendToTopic("/topic/kanban/" + boardId + "/card/" + cardId, newChecklistDTO);
        return newChecklistDTO;
    }

    public void deleteChecklist(UUID boardId, UUID cardId, UUID checklistId) {
        KanbanCard card = kanbanCardRepository.findByBoardIdAndId(boardId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        card.getChecklists().removeIf(checklist -> checklist.getId().equals(checklistId));
        kanbanCardRepository.save(card);
        notificationService.sendToTopic("/topic/kanban/" + boardId + "/card/" + cardId, Map.of("checklistId", checklistId, "deleted", true));
    }

    public void updateChecklist(UUID boardId, UUID cardId, UUID checklistId, ChecklistUpdateRequestDTO updateRequest) {
        KanbanCard card = kanbanCardRepository.findByBoardIdAndId(boardId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        Checklist checklist = card.getChecklists().stream()
                .filter(c -> c.getId().equals(checklistId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Checklist not found"));
        if (updateRequest.getTitle() != null) {
            checklist.setTitle(updateRequest.getTitle());
        }
        kanbanCardRepository.save(card);
        notificationService.sendToTopic("/topic/kanban/" + boardId + "/card/" + cardId, kanbanMapper.toDTO(checklist));
    }

    public ChecklistItemDTO addChecklistItem(UUID boardId, UUID cardId, UUID checklistId, ChecklistItemCreationRequestDTO creationRequest) {
        KanbanCard card = kanbanCardRepository.findByBoardIdAndId(boardId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        Checklist checklist = card.getChecklists().stream()
                .filter(c -> c.getId().equals(checklistId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Checklist not found"));
        ChecklistItem item = new ChecklistItem();
        item.setText(creationRequest.getText());
        checklist.getItems().add(item);
        kanbanCardRepository.save(card);
        ChecklistItemDTO checklistItemDTO = kanbanMapper.toDTO(item);
        notificationService.sendToTopic("/topic/kanban/" + boardId + "/card/" + cardId + "/checklist/" + checklistId, checklistItemDTO);
        return checklistItemDTO;
    }

    public void deleteChecklistItem(UUID boardId, UUID cardId, UUID checklistId, UUID itemId) {
        KanbanCard card = kanbanCardRepository.findByBoardIdAndId(boardId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        Checklist checklist = card.getChecklists().stream()
                .filter(c -> c.getId().equals(checklistId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Checklist not found"));
        checklist.getItems().removeIf(item -> item.getId().equals(itemId));
        kanbanCardRepository.save(card);
        notificationService.sendToTopic("/topic/kanban/" + boardId + "/card/" + cardId + "/checklist/" + checklistId, Map.of("itemId", itemId, "deleted", true));
    }

    public void updateChecklistItem(UUID boardId, UUID cardId, UUID checklistId, UUID itemId, ChecklistItemUpdateRequestDTO updateRequest) {
        KanbanCard card = kanbanCardRepository.findByBoardIdAndId(boardId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        Checklist checklist = card.getChecklists().stream()
                .filter(c -> c.getId().equals(checklistId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Checklist not found"));
        ChecklistItem item = checklist.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Checklist item not found"));
        if (updateRequest.getText() != null) {
            item.setText(updateRequest.getText());
        }
        if (updateRequest.getCompleted() != null) {
            item.setCompleted(updateRequest.getCompleted());
        }
        kanbanCardRepository.save(card);
        notificationService.sendToTopic("/topic/kanban/" + boardId + "/card/" + cardId + "/checklist/" + checklistId, kanbanMapper.toDTO(item));
    }

    public CardCommentDTO addComment(UUID boardId, UUID cardId, CommentCreationRequestDTO creationRequest, AppUserDetails userDetails) {
        KanbanCard card = kanbanCardRepository.findByBoardIdAndId(boardId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        Comment comment = new Comment();
        comment.setAuthorId(userDetails.getUserId());
        comment.setContent(creationRequest.getText());
        card.getComments().add(comment);
        kanbanCardRepository.save(card);
        CardCommentDTO cardCommentDTO = kanbanMapper.toDTO(comment);
        cardCommentDTO.setAuthor(userMapper.toDTO(userDetails.getUser()));
        notificationService.sendToTopic("/topic/kanban/" + boardId + "/card/" + cardId, cardCommentDTO);
        return cardCommentDTO;
    }

    public void deleteComment(UUID boardId, UUID cardId, UUID commentId) {
        KanbanCard card = kanbanCardRepository.findByBoardIdAndId(boardId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        card.getComments().removeIf(comment -> comment.getId().equals(commentId));
        kanbanCardRepository.save(card);
        notificationService.sendToTopic("/topic/kanban/" + boardId + "/card/" + cardId, Map.of("commentId", commentId, "deleted", true));
    }

    public void updateComment(UUID boardId, UUID cardId, UUID commentId, CommentUpdateRequestDTO updateRequest) {
        KanbanCard card = kanbanCardRepository.findByBoardIdAndId(boardId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        Comment comment = card.getComments().stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        if (updateRequest.getText() != null) {
            comment.setContent(updateRequest.getText());
            comment.setEdited(true);
        }
        kanbanCardRepository.save(card);
        CardCommentDTO cardCommentDTO = kanbanMapper.toDTO(comment);
        userRepository.findById(comment.getAuthorId()).ifPresent(user -> cardCommentDTO.setAuthor(userMapper.toDTO(user)));
        notificationService.sendToTopic("/topic/kanban/" + boardId + "/card/" + cardId, cardCommentDTO);
    }
}
