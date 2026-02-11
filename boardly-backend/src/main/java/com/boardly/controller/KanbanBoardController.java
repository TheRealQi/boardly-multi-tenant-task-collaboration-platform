package com.boardly.controller;

import com.boardly.commmon.dto.ApiSuccessResponseDTO;
import com.boardly.commmon.dto.kanbanboard.*;
import com.boardly.service.KanbanBoardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("${api.base-path}/${api.version}/kanban-board")
public class KanbanBoardController {
    KanbanBoardService kanbanBoardService;

    public KanbanBoardController(KanbanBoardService kanbanBoardService) {
        this.kanbanBoardService = kanbanBoardService;
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<ApiSuccessResponseDTO<KanbanBoardDTO>> getKanbanBoard(@PathVariable("boardId") UUID boardId) {
        KanbanBoardDTO kanbanBoardDTO = kanbanBoardService.getBoard(boardId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Kanban Board retrieved successfully", kanbanBoardDTO));
    }

    @PostMapping("/{boardId}/list")
    public ResponseEntity<ApiSuccessResponseDTO<KanbanListDTO>> createKanbanList(@PathVariable("boardId") UUID boardId, @Valid @RequestBody KanbanListCreationRequestDTO kanbanListCreationRequestDTO) {
        KanbanListDTO kanbanListDTO = kanbanBoardService.createList(boardId, kanbanListCreationRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponseDTO<>(HttpStatus.CREATED.value(), Instant.now(), "Kanban List created successfully", kanbanListDTO));
    }

    @PutMapping("/{boardId}/list/{listId}")
    public ResponseEntity<ApiSuccessResponseDTO<KanbanListDTO>> updateKanbanList(@PathVariable("boardId") UUID boardId, @PathVariable("listId") UUID listId, @Valid @RequestBody KanbanListUpdateRequestDTO kanbanListUpdateRequestDTO) {
        KanbanListDTO kanbanListDTO = kanbanBoardService.updateList(boardId, listId, kanbanListUpdateRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Kanban List updated successfully", kanbanListDTO));
    }

    @DeleteMapping("/{boardId}/list/{listId}")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> deleteKanbanList(@PathVariable("boardId") UUID boardId, @PathVariable("listId") UUID listId) {
        kanbanBoardService.deleteList(boardId, listId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Kanban List deleted successfully"));
    }

    // Kanban Card endpoints will be implemented here


    @PostMapping("/{boardId}/card")
    public ResponseEntity<ApiSuccessResponseDTO<KanbanCardDTO>> createKanbanCard(@PathVariable("boardId") UUID boardId, @Valid @RequestBody KanbanCardCreationRequestDTO kanbanCardCreationRequestDTO) {
        KanbanCardDTO kanbanCardDTO = kanbanBoardService.createCard(boardId, kanbanCardCreationRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponseDTO<>(HttpStatus.CREATED.value(), Instant.now(), "Kanban Card created successfully", kanbanCardDTO));
    }

}
