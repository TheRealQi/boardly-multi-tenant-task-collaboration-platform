package com.boardly.service;

import com.boardly.commmon.dto.kanbanboard.*;
import com.boardly.data.model.nosql.Checklist;
import com.boardly.data.model.nosql.KanbanBoard;
import com.boardly.data.model.nosql.KanbanCard;
import com.boardly.data.model.nosql.KanbanList;
import com.boardly.data.repository.KanbanBoardRepository;
import com.boardly.data.repository.KanbanCardRepository;
import com.boardly.exception.ResourceNotFoundException;
import com.boardly.security.model.AppUserDetails;
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
                    kanbanListDTO.setCards(kanbanCardDTOMap.getOrDefault(list.getId(), new ArrayList<>()));
                    return kanbanListDTO;
                }).toList();

        kanbanBoardDTO.setLists(kanbanListDTOs);
        return kanbanBoardDTO;
    }

    // List operations

    public void addList(UUID boardId, KanbanListCreationRequestDTO kanbanListCreationRequestDTO) {
        KanbanBoard kanbanBoard = kanbanBoardRepository.findByBoardId(boardId).orElseThrow(
                () -> new ResourceNotFoundException("Board not found"));
        KanbanList kanbanList = new KanbanList();
        kanbanList.setId(UUID.randomUUID());
        kanbanList.setTitle(kanbanListCreationRequestDTO.getTitle());
        kanbanList.setPosition(kanbanListCreationRequestDTO.getPosition());
        kanbanBoard.getLists().add(kanbanList);
        kanbanBoardRepository.save(kanbanBoard);
    }

    public void editListTitle(UUID boardId, UUID listId, KanbanListUpdateRequestDTO kanbanListUpdateRequestDTO) {
        KanbanBoard kanbanBoard = kanbanBoardRepository.findByBoardId(boardId).orElseThrow(
                () -> new ResourceNotFoundException("Board not found"));
        Optional<KanbanList> optionalKanbanList = kanbanBoard.getLists().stream()
                .filter(list -> list.getId().equals(listId))
                .findFirst();
        if (optionalKanbanList.isEmpty()) {
            throw new ResourceNotFoundException("List not found");
        }
        KanbanList kanbanList = optionalKanbanList.get();
        kanbanList.setTitle(kanbanListUpdateRequestDTO.getTitle());
        kanbanBoardRepository.save(kanbanBoard);
    }

    public KanbanListDTO moveList(UUID boardId, UUID listId, KanbanListMoveRequestDTO kanbanListMoveRequestDTO) {
        if (kanbanListMoveRequestDTO.getPosition() < 1) {
            throw new IllegalArgumentException("Position must be greater than 0");
        }

        if (kanbanListMoveRequestDTO.getPosition() > Long.MAX_VALUE) {
            throw new IllegalArgumentException("Position must be less than " + Long.MAX_VALUE);
        }

        KanbanBoard board = kanbanBoardRepository.findByBoardId(boardId).orElseThrow(
                () -> new ResourceNotFoundException("Board not found"));

        List<KanbanList> lists = board.getLists();
        KanbanList listToMove = lists.stream()
                .filter(l -> l.getId().equals(listId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("List not found"));

        double targetPosition = kanbanListMoveRequestDTO.getPosition();

        boolean collision = board.getLists().stream().anyMatch(list -> !list.getId().equals(listId) && list.getPosition() == targetPosition);
        if (collision) {
            rebalancePositions(board.getLists());
            kanbanBoardRepository.save(board);
        }

        listToMove.setPosition(targetPosition);
        kanbanBoardRepository.save(board);

        KanbanListDTO kanbanListDTO = new KanbanListDTO();
        kanbanListDTO.setListId(listToMove.getId());
        kanbanListDTO.setTitle(listToMove.getTitle());
        kanbanListDTO.setPosition(listToMove.getPosition());
        return kanbanListDTO;
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

}
