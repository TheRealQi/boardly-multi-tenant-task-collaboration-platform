package com.boardly.service;

import com.boardly.commmon.dto.kanbanboard.*;
import com.boardly.data.model.nosql.Checklist;
import com.boardly.data.model.nosql.KanbanBoard;
import com.boardly.data.model.nosql.KanbanCard;
import com.boardly.data.model.nosql.KanbanList;
import com.boardly.data.repository.KanbanBoardRepository;
import com.boardly.data.repository.KanbanCardRepository;
import com.boardly.exception.ResourceNotFoundException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KanbanBoardService {
    KanbanBoardRepository kanbanBoardRepository;
    KanbanCardRepository kanbanCardRepository;

    public KanbanBoardService(KanbanBoardRepository kanbanBoardRepository, KanbanCardRepository kanbanCardRepository) {
        this.kanbanCardRepository = kanbanCardRepository;
        this.kanbanBoardRepository = kanbanBoardRepository;
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

        KanbanBoardDTO kanbanBoardDTO = new KanbanBoardDTO();
        kanbanBoardDTO.setBoardId(kanbanBoard.getBoardId());
        List<KanbanCardDTO> kanbanCardDTOs = kanbanCardRepository.findAllByBoardId(boardId)
                .stream()
                .map(card -> {
                    KanbanCardDTO kanbanCardDTO = new KanbanCardDTO();
                    kanbanCardDTO.setCardId(card.getId());
                    kanbanCardDTO.setTitle(card.getTitle());
                    kanbanCardDTO.setListId(card.getListId());
                    kanbanCardDTO.setPosition(card.getPosition());
                    return kanbanCardDTO;
                }).toList();

        Map<UUID, List<KanbanCardDTO>> kanbanCardDTOMap = kanbanCardDTOs.stream()
                .collect(Collectors.groupingBy(KanbanCardDTO::getListId));

        List<KanbanListDTO> kanbanListDTOs = kanbanBoard.getLists().stream()
                .map(list -> {
                    KanbanListDTO kanbanListDTO = new KanbanListDTO();
                    kanbanListDTO.setListId(list.getId());
                    kanbanListDTO.setTitle(list.getTitle());
                    kanbanListDTO.setPosition(list.getPosition());
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

        KanbanListDTO kanbanListDTO = new KanbanListDTO();
        kanbanListDTO.setListId(kanbanList.getId());
        kanbanListDTO.setTitle(kanbanList.getTitle());
        kanbanListDTO.setPosition(kanbanList.getPosition());
        kanbanListDTO.setCards(new ArrayList<>());
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

        return new KanbanListDTO(list.getId(), list.getTitle(), list.getPosition(), null);
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
    }

    // Card operations


    public KanbanCardDTO createCard(UUID boardId, KanbanCardCreationRequestDTO kanbanCardCreationRequestDTO) {
        KanbanBoard kanbanBoard = kanbanBoardRepository.findByBoardId(boardId).orElseThrow(
                () -> new ResourceNotFoundException("Board not found"));

        UUID listId = kanbanCardCreationRequestDTO.getListId();
        KanbanList kanbanList = kanbanBoard.getLists().stream()
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

        KanbanCardDTO kanbanCardDTO = new KanbanCardDTO();
        kanbanCardDTO.setCardId(kanbanCard.getId());
        kanbanCardDTO.setTitle(kanbanCard.getTitle());
        kanbanCardDTO.setListId(kanbanCard.getListId());
        kanbanCardDTO.setPosition(kanbanCard.getPosition());
        return kanbanCardDTO;
    }

    public KanbanCardDTO getCard(UUID boardId, UUID cardId) {}


    // TODO: CRUD Implement assigning board members to cards, descriptions, due dates, labels, and checklists

}
