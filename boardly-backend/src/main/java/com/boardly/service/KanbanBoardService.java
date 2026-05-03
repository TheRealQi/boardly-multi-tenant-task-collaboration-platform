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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class KanbanBoardService {

    private static final double POSITION_GAP = 65536.0;
    private static final double POSITION_REBALANCE_THRESHOLD = 0.125;

    private final KanbanBoardRepository kanbanBoardRepository;
    private final KanbanCardRepository kanbanCardRepository;
    private final KanbanMapper kanbanMapper;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    public KanbanBoardService(
            KanbanBoardRepository kanbanBoardRepository,
            KanbanCardRepository kanbanCardRepository,
            KanbanMapper kanbanMapper,
            UserRepository userRepository,
            UserMapper userMapper,
            NotificationService notificationService) {
        this.kanbanBoardRepository = kanbanBoardRepository;
        this.kanbanCardRepository = kanbanCardRepository;
        this.kanbanMapper = kanbanMapper;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
    }

    // -------------------------------------------------------------------------
    // Board lifecycle
    // -------------------------------------------------------------------------

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
        KanbanBoard kanbanBoard = findBoardOrThrow(boardId);

        List<KanbanCardDTO> allCards = kanbanCardRepository.findAllByBoardId(boardId)
                .stream()
                .map(kanbanMapper::toDTO)
                .toList();

        Map<UUID, List<KanbanCardDTO>> cardsByList = allCards.stream()
                .collect(Collectors.groupingBy(KanbanCardDTO::getListId));

        List<KanbanListDTO> lists = kanbanBoard.getLists().stream()
                .map(list -> {
                    KanbanListDTO dto = kanbanMapper.toDTO(list);
                    List<KanbanCardDTO> cards = cardsByList.getOrDefault(list.getId(), new ArrayList<>());
                    cards.sort(Comparator.comparingDouble(KanbanCardDTO::getPosition));
                    dto.setCards(cards);
                    return dto;
                })
                .sorted(Comparator.comparingDouble(KanbanListDTO::getPosition))
                .toList();

        KanbanBoardDTO boardDTO = kanbanMapper.toDTO(kanbanBoard);
        boardDTO.setLists(lists);
        return boardDTO;
    }

    // -------------------------------------------------------------------------
    // List operations
    // -------------------------------------------------------------------------

    public KanbanListDTO createList(UUID boardId, KanbanListCreationRequestDTO request) {
        KanbanBoard board = findBoardOrThrow(boardId);

        KanbanList list = new KanbanList();
        list.setId(UUID.randomUUID());
        list.setTitle(request.getTitle());
        list.setPosition(nextListPosition(board));

        board.getLists().add(list);
        kanbanBoardRepository.save(board);

        KanbanListDTO dto = kanbanMapper.toDTO(list);
        dto.setCards(new ArrayList<>());

        notificationService.sendBoardEvent(boardId, BoardEvent.of(BoardEvent.Type.LIST_CREATED, dto));
        return dto;
    }

    public KanbanListDTO updateList(UUID boardId, UUID listId, KanbanListUpdateRequestDTO request) {
        if (request.getTitle() == null && request.getPosition() == null) {
            throw new IllegalArgumentException("At least one field (title or position) must be provided");
        }

        KanbanBoard board = findBoardOrThrow(boardId);
        KanbanList list = findListOrThrow(board, listId);

        if (request.getTitle() != null) {
            String trimmed = request.getTitle().trim();
            if (trimmed.isEmpty()) {
                throw new IllegalArgumentException("Title must not be blank");
            }
            list.setTitle(trimmed);
        }

        if (request.getPosition() != null) {
            list.setPosition(resolveListPosition(board, list, request.getPosition()));
        }

        kanbanBoardRepository.save(board);

        KanbanListDTO dto = kanbanMapper.toDTO(list);
        notificationService.sendBoardEvent(boardId, BoardEvent.of(BoardEvent.Type.LIST_UPDATED, dto));
        return dto;
    }

    public void deleteList(UUID boardId, UUID listId) {
        KanbanBoard board = findBoardOrThrow(boardId);
        boolean removed = board.getLists().removeIf(l -> l.getId() != null && l.getId().equals(listId));
        if (!removed) {
            throw new ResourceNotFoundException("List not found");
        }
        kanbanCardRepository.deleteAllByBoardIdAndListId(boardId, listId);
        kanbanBoardRepository.save(board);

        notificationService.sendBoardEvent(boardId,
                BoardEvent.of(BoardEvent.Type.LIST_DELETED, Map.of("listId", listId)));
    }

    // -------------------------------------------------------------------------
    // Card operations
    // -------------------------------------------------------------------------

    public KanbanCardDTO createCard(UUID boardId, KanbanCardCreationRequestDTO request) {
        KanbanBoard board = findBoardOrThrow(boardId);
        UUID listId = request.getListId();
        findListOrThrow(board, listId); // validate list exists

        KanbanCard card = new KanbanCard();
        card.setId(UUID.randomUUID());
        card.setBoardId(boardId);
        card.setListId(listId);
        card.setTitle(request.getTitle());
        card.setPosition(nextCardPosition(boardId, listId));

        kanbanCardRepository.save(card);

        KanbanCardDTO dto = kanbanMapper.toDTO(card);
        notificationService.sendBoardEvent(boardId, BoardEvent.of(BoardEvent.Type.CARD_CREATED, dto));
        return dto;
    }

    public void deleteCard(UUID boardId, UUID cardId) {
        KanbanCard card = findCardOrThrow(boardId, cardId);
        kanbanCardRepository.delete(card);
        notificationService.sendBoardEvent(boardId,
                BoardEvent.of(BoardEvent.Type.CARD_DELETED, Map.of("cardId", cardId)));
    }

    public KanbanCardDetailsDTO getCard(UUID boardId, UUID cardId) {
        KanbanCard card = findCardOrThrow(boardId, cardId);
        KanbanCardDetailsDTO dto = kanbanMapper.toDetailsDTO(card);

        // Batch-fetch all comment authors in a single query
        Set<UUID> authorIds = card.getComments().stream()
                .map(Comment::getAuthorId)
                .collect(Collectors.toSet());

        Map<UUID, com.boardly.data.model.sql.authentication.User> usersById =
                userRepository.findAllById(authorIds).stream()
                        .collect(Collectors.toMap(
                                com.boardly.data.model.sql.authentication.User::getId,
                                Function.identity()));

        List<CardCommentDTO> comments = card.getComments().stream()
                .map(comment -> {
                    CardCommentDTO commentDTO = kanbanMapper.toDTO(comment);
                    Optional.ofNullable(usersById.get(comment.getAuthorId()))
                            .ifPresent(user -> commentDTO.setAuthor(userMapper.toDTO(user)));
                    return commentDTO;
                })
                .toList();

        dto.setComments(comments);
        return dto;
    }

    public void updateCard(UUID boardId, UUID cardId, KanbanCardUpdateRequestDTO request) {
        KanbanCard card = findCardOrThrow(boardId, cardId);

        if (request.getTitle() != null)           card.setTitle(request.getTitle());
        if (request.getDescription() != null)      card.setDescription(request.getDescription());
        if (request.getStartDate() != null)        card.setStartDate(request.getStartDate());
        if (request.getDueDate() != null)          card.setDueDate(request.getDueDate());
        if (request.getLabels() != null)           card.setLabels(request.getLabels());
        if (request.getAssignedMembers() != null)  card.setAssignedMembers(new HashSet<>(request.getAssignedMembers()));

        if (request.getPosition() != null || request.getListId() != null) {
            double targetPosition = request.getPosition() != null ? request.getPosition() : card.getPosition();
            applyCardMove(boardId, card, targetPosition, request.getListId());
        }

        kanbanCardRepository.save(card);
        notificationService.sendBoardEvent(boardId, BoardEvent.of(BoardEvent.Type.CARD_UPDATED, kanbanMapper.toDTO(card)));
    }

    // -------------------------------------------------------------------------
    // Checklist operations
    // -------------------------------------------------------------------------

    public ChecklistDTO addChecklist(UUID boardId, UUID cardId, ChecklistDTO request) {
        KanbanCard card = findCardOrThrow(boardId, cardId);

        Checklist checklist = new Checklist();
        checklist.setId(UUID.randomUUID());
        checklist.setTitle(request.getTitle());
        checklist.setItems(new ArrayList<>());
        card.getChecklists().add(checklist);
        kanbanCardRepository.save(card);

        ChecklistDTO dto = kanbanMapper.toDTO(checklist);
        notificationService.sendBoardEvent(boardId, BoardEvent.of(BoardEvent.Type.CHECKLIST_ADDED, dto));
        return dto;
    }

    public void updateChecklist(UUID boardId, UUID cardId, UUID checklistId, ChecklistUpdateRequestDTO request) {
        KanbanCard card = findCardOrThrow(boardId, cardId);
        Checklist checklist = findChecklistOrThrow(card, checklistId);

        if (request.getTitle() != null) {
            checklist.setTitle(request.getTitle());
        }
        kanbanCardRepository.save(card);

        notificationService.sendBoardEvent(boardId, BoardEvent.of(BoardEvent.Type.CHECKLIST_UPDATED, kanbanMapper.toDTO(checklist)));
    }

    public void deleteChecklist(UUID boardId, UUID cardId, UUID checklistId) {
        KanbanCard card = findCardOrThrow(boardId, cardId);
        boolean removed = card.getChecklists().removeIf(c -> c.getId() != null && c.getId().equals(checklistId));
        if (!removed) {
            throw new ResourceNotFoundException("Checklist not found");
        }
        kanbanCardRepository.save(card);

        notificationService.sendBoardEvent(boardId,
                BoardEvent.of(BoardEvent.Type.CHECKLIST_DELETED, Map.of("checklistId", checklistId)));
    }

    // -------------------------------------------------------------------------
    // Checklist item operations
    // -------------------------------------------------------------------------

    public ChecklistItemDTO addChecklistItem(UUID boardId, UUID cardId, UUID checklistId, ChecklistItemCreationRequestDTO request) {
        KanbanCard card = findCardOrThrow(boardId, cardId);
        Checklist checklist = findChecklistOrThrow(card, checklistId);

        ChecklistItem item = new ChecklistItem();
        item.setText(request.getText());
        checklist.getItems().add(item);
        kanbanCardRepository.save(card);

        ChecklistItemDTO dto = kanbanMapper.toDTO(item);
        notificationService.sendBoardEvent(boardId, BoardEvent.of(BoardEvent.Type.CHECKLIST_ITEM_ADDED, dto));
        return dto;
    }

    public void updateChecklistItem(UUID boardId, UUID cardId, UUID checklistId, UUID itemId, ChecklistItemUpdateRequestDTO request) {
        KanbanCard card = findCardOrThrow(boardId, cardId);
        Checklist checklist = findChecklistOrThrow(card, checklistId);
        ChecklistItem item = findChecklistItemOrThrow(checklist, itemId);

        if (request.getText() != null)      item.setText(request.getText());
        if (request.getCompleted() != null) item.setCompleted(request.getCompleted());

        kanbanCardRepository.save(card);
        notificationService.sendBoardEvent(boardId, BoardEvent.of(BoardEvent.Type.CHECKLIST_ITEM_UPDATED, kanbanMapper.toDTO(item)));
    }

    public void deleteChecklistItem(UUID boardId, UUID cardId, UUID checklistId, UUID itemId) {
        KanbanCard card = findCardOrThrow(boardId, cardId);
        Checklist checklist = findChecklistOrThrow(card, checklistId);
        boolean removed = checklist.getItems().removeIf(i -> i.getId() != null && i.getId().equals(itemId));
        if (!removed) {
            throw new ResourceNotFoundException("Checklist item not found");
        }
        kanbanCardRepository.save(card);

        notificationService.sendBoardEvent(boardId,
                BoardEvent.of(BoardEvent.Type.CHECKLIST_ITEM_DELETED, Map.of("itemId", itemId)));
    }

    // -------------------------------------------------------------------------
    // Comment operations
    // -------------------------------------------------------------------------

    public CardCommentDTO addComment(UUID boardId, UUID cardId, CommentCreationRequestDTO request, AppUserDetails userDetails) {
        KanbanCard card = findCardOrThrow(boardId, cardId);

        Comment comment = new Comment();
        comment.setAuthorId(userDetails.getUserId());
        comment.setContent(request.getText());
        card.getComments().add(comment);
        kanbanCardRepository.save(card);

        CardCommentDTO dto = kanbanMapper.toDTO(comment);
        dto.setAuthor(userMapper.toDTO(userDetails.getUser()));

        notificationService.sendBoardEvent(boardId, BoardEvent.of(BoardEvent.Type.COMMENT_ADDED, dto));
        return dto;
    }

    public void updateComment(UUID boardId, UUID cardId, UUID commentId, CommentUpdateRequestDTO request, UUID requestingUserId) {
        KanbanCard card = findCardOrThrow(boardId, cardId);
        Comment comment = findCommentOrThrow(card, commentId);

        if (!comment.getAuthorId().equals(requestingUserId)) {
            throw new AccessDeniedException("You can only edit your own comments");
        }

        if (request.getText() != null) {
            comment.setContent(request.getText());
            comment.setEdited(true);
        }
        kanbanCardRepository.save(card);

        CardCommentDTO dto = kanbanMapper.toDTO(comment);
        Optional.ofNullable(userRepository.findById(comment.getAuthorId()).orElse(null))
                .ifPresent(user -> dto.setAuthor(userMapper.toDTO(user)));

        notificationService.sendBoardEvent(boardId, BoardEvent.of(BoardEvent.Type.COMMENT_UPDATED, dto));
    }

    public void deleteComment(UUID boardId, UUID cardId, UUID commentId, UUID requestingUserId) {
        KanbanCard card = findCardOrThrow(boardId, cardId);
        Comment comment = findCommentOrThrow(card, commentId);

        if (!comment.getAuthorId().equals(requestingUserId)) {
            throw new AccessDeniedException("You can only delete your own comments");
        }

        card.getComments().removeIf(c -> c.getId() != null && c.getId().equals(commentId));
        kanbanCardRepository.save(card);

        notificationService.sendBoardEvent(boardId,
                BoardEvent.of(BoardEvent.Type.COMMENT_DELETED, Map.of("commentId", commentId)));
    }

    // -------------------------------------------------------------------------
    // Private helpers — lookup
    // -------------------------------------------------------------------------

    private KanbanBoard findBoardOrThrow(UUID boardId) {
        return kanbanBoardRepository.findByBoardId(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));
    }

    private KanbanCard findCardOrThrow(UUID boardId, UUID cardId) {
        return kanbanCardRepository.findByBoardIdAndId(boardId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
    }

    private KanbanList findListOrThrow(KanbanBoard board, UUID listId) {
        return board.getLists().stream()
                .filter(l -> l.getId() != null && l.getId().equals(listId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("List not found"));
    }

    private Checklist findChecklistOrThrow(KanbanCard card, UUID checklistId) {
        return card.getChecklists().stream()
                .filter(c -> c.getId() != null && c.getId().equals(checklistId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Checklist not found"));
    }

    private ChecklistItem findChecklistItemOrThrow(Checklist checklist, UUID itemId) {
        return checklist.getItems().stream()
                .filter(i -> i.getId() != null && i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Checklist item not found"));
    }

    private Comment findCommentOrThrow(KanbanCard card, UUID commentId) {
        return card.getComments().stream()
                .filter(c -> c.getId() != null && c.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
    }

    // -------------------------------------------------------------------------
    // Private helpers — position management
    // -------------------------------------------------------------------------

    private double nextListPosition(KanbanBoard board) {
        double max = board.getLists().stream()
                .mapToDouble(KanbanList::getPosition)
                .max()
                .orElse(0.0);
        double next = max + POSITION_GAP;
        if (next >= Double.MAX_VALUE) {
            rebalanceListPositions(board.getLists());
            next = board.getLists().stream()
                    .mapToDouble(KanbanList::getPosition)
                    .max()
                    .orElse(0.0) + POSITION_GAP;
        }
        return next;
    }

    private double nextCardPosition(UUID boardId, UUID listId) {
        double max = kanbanCardRepository.findAllByBoardIdAndListId(boardId, listId).stream()
                .mapToDouble(KanbanCard::getPosition)
                .max()
                .orElse(0.0);
        double next = max + POSITION_GAP;
        if (next >= Double.MAX_VALUE) {
            rebalanceCardPositions(boardId, listId);
            next = kanbanCardRepository.findAllByBoardIdAndListId(boardId, listId).stream()
                    .mapToDouble(KanbanCard::getPosition)
                    .max()
                    .orElse(0.0) + POSITION_GAP;
        }
        return next;
    }

    /**
     * Returns the resolved position for a list move, rebalancing in-memory if needed.
     * The caller is responsible for saving the board afterwards.
     */
    private double resolveListPosition(KanbanBoard board, KanbanList listToMove, double targetPosition) {
        if (targetPosition < 0) {
            throw new IllegalArgumentException("Position must be non-negative");
        }

        listToMove.setPosition(targetPosition);

        boolean needsRebalance = targetPosition >= Double.MAX_VALUE
                || targetPosition < POSITION_REBALANCE_THRESHOLD
                || hasListPositionCollision(board.getLists(), targetPosition, listToMove.getId());

        if (needsRebalance) {
            rebalanceListPositions(board.getLists());
            return board.getLists().stream()
                    .filter(l -> l.getId().equals(listToMove.getId()))
                    .findFirst()
                    .map(KanbanList::getPosition)
                    .orElseThrow(() -> new ResourceNotFoundException("List not found after rebalancing"));
        }

        return targetPosition;
    }

    /**
     * Applies a card move (possibly to a different list) and resolves position.
     * May persist a rebalance; caller must save the card afterwards for non-rebalance field updates.
     */
    private void applyCardMove(UUID boardId, KanbanCard card, double targetPosition, UUID targetListId) {
        if (targetPosition < 0) {
            throw new IllegalArgumentException("Position must be non-negative");
        }

        if (targetListId != null) {
            // Validate the target list exists
            findListOrThrow(findBoardOrThrow(boardId), targetListId);
            card.setListId(targetListId);
        }

        UUID effectiveListId = card.getListId();

        boolean needsRebalance = targetPosition >= Double.MAX_VALUE
                || targetPosition < POSITION_REBALANCE_THRESHOLD
                || isCardPositionCollision(boardId, effectiveListId, targetPosition, card.getId());

        if (needsRebalance) {
            card.setPosition(targetPosition);
            kanbanCardRepository.save(card);
            rebalanceCardPositions(boardId, effectiveListId);
            KanbanCard rebalanced = findCardOrThrow(boardId, card.getId());
            card.setPosition(rebalanced.getPosition());
        } else {
            card.setPosition(targetPosition);
        }
    }

    private boolean hasListPositionCollision(List<KanbanList> lists, double target, UUID excludeId) {
        return lists.stream()
                .filter(l -> l.getId() != null && !l.getId().equals(excludeId))
                .anyMatch(l -> Math.abs(l.getPosition() - target) < 0.0001);
    }

    private boolean isCardPositionCollision(UUID boardId, UUID listId, double target, UUID excludeId) {
        return kanbanCardRepository.findAllByBoardIdAndListId(boardId, listId).stream()
                .filter(c -> !c.getId().equals(excludeId))
                .anyMatch(c -> Math.abs(c.getPosition() - target) < 0.0001);
    }

    private void rebalanceListPositions(List<KanbanList> lists) {
        lists.sort(Comparator.comparingDouble(KanbanList::getPosition));
        double pos = POSITION_GAP;
        for (KanbanList l : lists) {
            l.setPosition(pos);
            pos += POSITION_GAP;
        }
    }

    private void rebalanceCardPositions(UUID boardId, UUID listId) {
        List<KanbanCard> cards = kanbanCardRepository.findAllByBoardIdAndListId(boardId, listId);
        cards.sort(Comparator.comparingDouble(KanbanCard::getPosition));
        double pos = POSITION_GAP;
        for (KanbanCard c : cards) {
            c.setPosition(pos);
            pos += POSITION_GAP;
        }
        kanbanCardRepository.saveAll(cards);
    }
}