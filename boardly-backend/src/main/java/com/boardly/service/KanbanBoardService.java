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

    private static final double MIN_POSITION       = 0.125;
    private static final double COLLISION_THRESHOLD = 0.0001;
    private static final double REBALANCE_START    = Math.pow(2, 47);
    private static final double REBALANCE_STEP     = Math.pow(2, 14);

    private final KanbanBoardRepository kanbanBoardRepository;
    private final KanbanCardRepository  kanbanCardRepository;
    private final KanbanMapper          kanbanMapper;
    private final UserRepository        userRepository;
    private final UserMapper            userMapper;
    private final NotificationService   notificationService;

    public KanbanBoardService(
            KanbanBoardRepository kanbanBoardRepository,
            KanbanCardRepository  kanbanCardRepository,
            KanbanMapper          kanbanMapper,
            UserRepository        userRepository,
            UserMapper            userMapper,
            NotificationService   notificationService) {
        this.kanbanBoardRepository = kanbanBoardRepository;
        this.kanbanCardRepository  = kanbanCardRepository;
        this.kanbanMapper          = kanbanMapper;
        this.userRepository        = userRepository;
        this.userMapper            = userMapper;
        this.notificationService   = notificationService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Board
    // ─────────────────────────────────────────────────────────────────────────

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
        KanbanBoard kanbanBoard = kanbanBoardRepository.findByBoardId(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

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

    // ─────────────────────────────────────────────────────────────────────────
    // List operations
    // ─────────────────────────────────────────────────────────────────────────

    public KanbanListDTO createList(UUID boardId, KanbanListCreationRequestDTO request) {
        KanbanBoard board = kanbanBoardRepository.findByBoardId(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        KanbanList kanbanList = new KanbanList();
        kanbanList.setId(UUID.randomUUID());
        kanbanList.setTitle(request.getTitle());

        // FIX: resolve position BEFORE adding the new list so it cannot collide
        // with itself. excludeId=null because the list isn't in the collection yet.
        double resolvedPosition = resolveListPosition(board.getLists(), request.getPosition(), null);
        kanbanList.setPosition(resolvedPosition);

        board.getLists().add(kanbanList);
        kanbanBoardRepository.save(board); // single save

        KanbanListDTO kanbanListDTO = kanbanMapper.toDTO(kanbanList);
        kanbanListDTO.setCards(new ArrayList<>());
        notificationService.sendToTopic("/topic/kanban/" + boardId, kanbanListDTO);
        return kanbanListDTO;
    }

    public KanbanListDTO updateList(UUID boardId, UUID listId, KanbanListUpdateRequestDTO request) {
        KanbanBoard board = kanbanBoardRepository.findByBoardId(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        KanbanList list = board.getLists().stream()
                .filter(l -> l.getId().equals(listId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("List not found"));

        if (request.getTitle() == null && request.getPosition() == null) {
            throw new IllegalArgumentException("At least one field (title or position) must be provided");
        }

        if (request.getTitle() != null) {
            String newTitle = request.getTitle().trim();
            if (newTitle.isEmpty()) throw new IllegalArgumentException("Title cannot be empty");
            list.setTitle(newTitle);
        }

        if (request.getPosition() != null) {
            // FIX: pass listId so the list's current position isn't treated as a
            // collision against itself. resolveListPosition mutates sibling positions
            // in-place when rebalancing is needed, so all changes land in one save.
            double resolvedPosition = resolveListPosition(board.getLists(), request.getPosition(), listId);
            list.setPosition(resolvedPosition);
        }

        kanbanBoardRepository.save(board);

        KanbanListDTO dto = kanbanMapper.toDTO(list);
        notificationService.sendToTopic("/topic/kanban/" + boardId, dto);
        return dto;
    }

    private double resolveListPosition(List<KanbanList> allLists, double targetPosition, UUID excludeId) {
        if (targetPosition < 0) {
            throw new IllegalArgumentException("Position must be >= 0");
        }
        if (targetPosition > Long.MAX_VALUE) {
            throw new IllegalArgumentException("Position must be less than " + Long.MAX_VALUE);
        }

        List<KanbanList> others = allLists.stream()
                .filter(l -> excludeId == null || !l.getId().equals(excludeId))
                .sorted(Comparator.comparingDouble(KanbanList::getPosition))
                .collect(Collectors.toList());

        boolean collision = others.stream()
                .anyMatch(l -> Math.abs(l.getPosition() - targetPosition) < COLLISION_THRESHOLD);

        if (!collision && targetPosition >= MIN_POSITION) {
            return targetPosition;
        }
        KanbanList prevNeighbour = null;
        KanbanList nextNeighbour = null;
        for (KanbanList l : others) {
            if (l.getPosition() <= targetPosition) {
                prevNeighbour = l;
            } else {
                nextNeighbour = l;
                break;
            }
        }

        UUID prevId = prevNeighbour != null ? prevNeighbour.getId() : null;
        UUID nextId = nextNeighbour != null ? nextNeighbour.getId() : null;

        rebalanceListPositions(others);

        double newPrev = prevId == null ? 0.0
                : others.stream()
                        .filter(l -> l.getId().equals(prevId))
                        .mapToDouble(KanbanList::getPosition)
                        .findFirst().orElse(0.0);

        double newNext = nextId == null ? -1.0
                : others.stream()
                        .filter(l -> l.getId().equals(nextId))
                        .mapToDouble(KanbanList::getPosition)
                        .findFirst().orElse(-1.0);

        if (nextId == null || newNext < 0) {
            return (newPrev == 0.0) ? REBALANCE_START : newPrev + REBALANCE_STEP;
        } else if (prevId == null) {
            return newNext / 2.0;
        } else {
            return (newPrev + newNext) / 2.0;
        }
    }

    private void rebalanceListPositions(List<KanbanList> lists) {
        lists.sort(Comparator.comparingDouble(KanbanList::getPosition));
        double pos = REBALANCE_START;
        for (KanbanList l : lists) {
            l.setPosition(pos);
            pos += REBALANCE_STEP;
        }
    }

    public void deleteList(UUID boardId, UUID listId) {
        KanbanBoard kanbanBoard = kanbanBoardRepository.findByBoardId(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));
        boolean removed = kanbanBoard.getLists().removeIf(list -> list.getId().equals(listId));
        if (!removed) throw new ResourceNotFoundException("List not found");
        kanbanCardRepository.deleteAllByBoardIdAndListId(boardId, listId);
        kanbanBoardRepository.save(kanbanBoard);
        notificationService.sendToTopic("/topic/kanban/" + boardId,
                Map.of("listId", listId, "deleted", true));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Card operations
    // ─────────────────────────────────────────────────────────────────────────

    public KanbanCardDTO createCard(UUID boardId, KanbanCardCreationRequestDTO request) {
        KanbanBoard board = kanbanBoardRepository.findByBoardId(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        UUID listId = request.getListId();
        board.getLists().stream()
                .filter(l -> l.getId().equals(listId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("List not found"));

        List<KanbanCard> existingCards = kanbanCardRepository.findAllByBoardIdAndListId(boardId, listId);
        double resolvedPosition = resolveCardPosition(boardId, existingCards, request.getPosition(), null);

        KanbanCard card = new KanbanCard();
        card.setId(UUID.randomUUID());
        card.setBoardId(boardId);
        card.setListId(listId);
        card.setTitle(request.getTitle());
        card.setPosition(resolvedPosition);
        kanbanCardRepository.save(card);

        KanbanCardDTO dto = kanbanMapper.toDTO(card);
        notificationService.sendToTopic("/topic/kanban/" + boardId, dto);
        return dto;
    }

    public KanbanCardDetailsDTO getCard(UUID boardId, UUID cardId) {
        KanbanCard card = kanbanCardRepository.findByBoardIdAndId(boardId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        KanbanCardDetailsDTO dto = kanbanMapper.toDetailsDTO(card);
        List<CardCommentDTO> comments = card.getComments().stream()
                .map(comment -> {
                    CardCommentDTO commentDTO = kanbanMapper.toDTO(comment);
                    userRepository.findById(comment.getAuthorId())
                            .ifPresent(user -> commentDTO.setAuthor(userMapper.toDTO(user)));
                    return commentDTO;
                })
                .collect(Collectors.toList());
        dto.setComments(comments);
        return dto;
    }

    public void updateCard(UUID boardId, UUID cardId, KanbanCardUpdateRequestDTO updateRequest) {
        KanbanCard card = kanbanCardRepository.findByBoardIdAndId(boardId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        if (updateRequest.getTitle()           != null) card.setTitle(updateRequest.getTitle());
        if (updateRequest.getDescription()     != null) card.setDescription(updateRequest.getDescription());
        if (updateRequest.getStartDate()       != null) card.setStartDate(updateRequest.getStartDate());
        if (updateRequest.getDueDate()         != null) card.setDueDate(updateRequest.getDueDate());
        if (updateRequest.getLabels()          != null) card.setLabels(updateRequest.getLabels());
        if (updateRequest.getAssignedMembers() != null) card.setAssignedMembers(new HashSet<>(updateRequest.getAssignedMembers()));

        boolean movingToNewList = updateRequest.getListId() != null
                && !updateRequest.getListId().equals(card.getListId());

        if (movingToNewList) {
            kanbanBoardRepository.findByBoardId(boardId)
                    .orElseThrow(() -> new ResourceNotFoundException("Board not found"))
                    .getLists().stream()
                    .filter(l -> l.getId().equals(updateRequest.getListId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Target list not found"));
            card.setListId(updateRequest.getListId());
        }

        if (updateRequest.getPosition() != null) {
            UUID targetListId = card.getListId(); // already updated above if moving

            List<KanbanCard> targetCards = kanbanCardRepository
                    .findAllByBoardIdAndListId(boardId, targetListId)
                    .stream()
                    .filter(c -> !c.getId().equals(cardId))
                    .collect(Collectors.toList());

            double resolvedPosition = resolveCardPosition(boardId, targetCards, updateRequest.getPosition(), null);
            card.setPosition(resolvedPosition);
        }

        kanbanCardRepository.save(card);
        notificationService.sendToTopic("/topic/kanban/" + boardId, kanbanMapper.toDTO(card));
    }

    private double resolveCardPosition(UUID boardId, List<KanbanCard> siblings,
                                       double targetPosition, UUID excludeId) {
        if (targetPosition < 0) {
            throw new IllegalArgumentException("Position must be >= 0");
        }

        List<KanbanCard> others = siblings.stream()
                .filter(c -> excludeId == null || !c.getId().equals(excludeId))
                .sorted(Comparator.comparingDouble(KanbanCard::getPosition))
                .collect(Collectors.toList());

        boolean collision = others.stream()
                .anyMatch(c -> Math.abs(c.getPosition() - targetPosition) < COLLISION_THRESHOLD);

        // Happy path.
        if (!collision && targetPosition >= MIN_POSITION) {
            return targetPosition;
        }

        // Identify neighbours before rebalancing.
        KanbanCard prevNeighbour = null;
        KanbanCard nextNeighbour = null;
        for (KanbanCard c : others) {
            if (c.getPosition() <= targetPosition) {
                prevNeighbour = c;
            } else {
                nextNeighbour = c;
                break;
            }
        }

        UUID prevId = prevNeighbour != null ? prevNeighbour.getId() : null;
        UUID nextId = nextNeighbour != null ? nextNeighbour.getId() : null;

        rebalanceCardPositions(others);
        kanbanCardRepository.saveAll(others);

        double newPrev = prevId == null ? 0.0
                : others.stream()
                        .filter(c -> c.getId().equals(prevId))
                        .mapToDouble(KanbanCard::getPosition)
                        .findFirst().orElse(0.0);

        double newNext = nextId == null ? -1.0
                : others.stream()
                        .filter(c -> c.getId().equals(nextId))
                        .mapToDouble(KanbanCard::getPosition)
                        .findFirst().orElse(-1.0);

        if (nextId == null || newNext < 0) {
            return (newPrev == 0.0) ? REBALANCE_START : newPrev + REBALANCE_STEP;
        } else if (prevId == null) {
            return newNext / 2.0;
        } else {
            return (newPrev + newNext) / 2.0;
        }
    }

    private void rebalanceCardPositions(List<KanbanCard> cards) {
        cards.sort(Comparator.comparingDouble(KanbanCard::getPosition));
        double pos = REBALANCE_START;
        for (KanbanCard c : cards) {
            c.setPosition(pos);
            pos += REBALANCE_STEP;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Checklist operations
    // ─────────────────────────────────────────────────────────────────────────

    public ChecklistDTO addChecklist(UUID boardId, UUID cardId, ChecklistDTO checklistDTO) {
        KanbanCard card = kanbanCardRepository.findByBoardIdAndId(boardId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        Checklist checklist = new Checklist();
        checklist.setId(UUID.randomUUID());
        checklist.setTitle(checklistDTO.getTitle());
        checklist.setItems(new ArrayList<>());
        card.getChecklists().add(checklist);
        kanbanCardRepository.save(card);
        ChecklistDTO dto = kanbanMapper.toDTO(checklist);
        notificationService.sendToTopic("/topic/kanban/" + boardId + "/card/" + cardId, dto);
        return dto;
    }

    public void deleteChecklist(UUID boardId, UUID cardId, UUID checklistId) {
        KanbanCard card = kanbanCardRepository.findByBoardIdAndId(boardId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        card.getChecklists().removeIf(c -> c.getId().equals(checklistId));
        kanbanCardRepository.save(card);
        notificationService.sendToTopic("/topic/kanban/" + boardId + "/card/" + cardId,
                Map.of("checklistId", checklistId, "deleted", true));
    }

    public void updateChecklist(UUID boardId, UUID cardId, UUID checklistId,
                                ChecklistUpdateRequestDTO updateRequest) {
        KanbanCard card = kanbanCardRepository.findByBoardIdAndId(boardId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        Checklist checklist = card.getChecklists().stream()
                .filter(c -> c.getId().equals(checklistId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Checklist not found"));
        if (updateRequest.getTitle() != null) checklist.setTitle(updateRequest.getTitle());
        kanbanCardRepository.save(card);
        notificationService.sendToTopic("/topic/kanban/" + boardId + "/card/" + cardId,
                kanbanMapper.toDTO(checklist));
    }

    public ChecklistItemDTO addChecklistItem(UUID boardId, UUID cardId, UUID checklistId,
                                             ChecklistItemCreationRequestDTO creationRequest) {
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
        ChecklistItemDTO dto = kanbanMapper.toDTO(item);
        notificationService.sendToTopic(
                "/topic/kanban/" + boardId + "/card/" + cardId + "/checklist/" + checklistId, dto);
        return dto;
    }

    public void deleteChecklistItem(UUID boardId, UUID cardId, UUID checklistId, UUID itemId) {
        KanbanCard card = kanbanCardRepository.findByBoardIdAndId(boardId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        Checklist checklist = card.getChecklists().stream()
                .filter(c -> c.getId().equals(checklistId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Checklist not found"));
        checklist.getItems().removeIf(i -> i.getId().equals(itemId));
        kanbanCardRepository.save(card);
        notificationService.sendToTopic(
                "/topic/kanban/" + boardId + "/card/" + cardId + "/checklist/" + checklistId,
                Map.of("itemId", itemId, "deleted", true));
    }

    public void updateChecklistItem(UUID boardId, UUID cardId, UUID checklistId, UUID itemId,
                                    ChecklistItemUpdateRequestDTO updateRequest) {
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
        if (updateRequest.getText()      != null) item.setText(updateRequest.getText());
        if (updateRequest.getCompleted() != null) item.setCompleted(updateRequest.getCompleted());
        kanbanCardRepository.save(card);
        notificationService.sendToTopic(
                "/topic/kanban/" + boardId + "/card/" + cardId + "/checklist/" + checklistId,
                kanbanMapper.toDTO(item));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Comment operations
    // ─────────────────────────────────────────────────────────────────────────

    public CardCommentDTO addComment(UUID boardId, UUID cardId,
                                     CommentCreationRequestDTO creationRequest,
                                     AppUserDetails userDetails) {
        KanbanCard card = kanbanCardRepository.findByBoardIdAndId(boardId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        Comment comment = new Comment();
        comment.setAuthorId(userDetails.getUserId());
        comment.setContent(creationRequest.getText());
        card.getComments().add(comment);
        kanbanCardRepository.save(card);
        CardCommentDTO dto = kanbanMapper.toDTO(comment);
        dto.setAuthor(userMapper.toDTO(userDetails.getUser()));
        notificationService.sendToTopic("/topic/kanban/" + boardId + "/card/" + cardId, dto);
        return dto;
    }

    public void deleteComment(UUID boardId, UUID cardId, UUID commentId) {
        KanbanCard card = kanbanCardRepository.findByBoardIdAndId(boardId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        card.getComments().removeIf(c -> c.getId().equals(commentId));
        kanbanCardRepository.save(card);
        notificationService.sendToTopic("/topic/kanban/" + boardId + "/card/" + cardId,
                Map.of("commentId", commentId, "deleted", true));
    }

    public void updateComment(UUID boardId, UUID cardId, UUID commentId,
                              CommentUpdateRequestDTO updateRequest) {
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
        CardCommentDTO dto = kanbanMapper.toDTO(comment);
        userRepository.findById(comment.getAuthorId())
                .ifPresent(user -> dto.setAuthor(userMapper.toDTO(user)));
        notificationService.sendToTopic("/topic/kanban/" + boardId + "/card/" + cardId, dto);
    }
}
