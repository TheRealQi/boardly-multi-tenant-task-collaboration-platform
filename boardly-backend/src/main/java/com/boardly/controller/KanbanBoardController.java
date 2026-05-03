package com.boardly.controller;

import com.boardly.common.dto.ApiSuccessResponseDTO;
import com.boardly.common.dto.kanbanboard.*;
import com.boardly.security.model.AppUserDetails;
import com.boardly.service.KanbanBoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("${api.base-path}/${api.version}/kanban-board")
@Tag(name = "Kanban Board")
public class KanbanBoardController {

    private final KanbanBoardService kanbanBoardService;

    public KanbanBoardController(KanbanBoardService kanbanBoardService) {
        this.kanbanBoardService = kanbanBoardService;
    }

    // -------------------------------------------------------------------------
    // Board
    // -------------------------------------------------------------------------

    @Operation(summary = "Get a kanban board by ID", responses = {
            @ApiResponse(description = "Success",      responseCode = "200"),
            @ApiResponse(description = "Unauthorized", responseCode = "401"),
            @ApiResponse(description = "Forbidden",    responseCode = "403"),
            @ApiResponse(description = "Not found",    responseCode = "404")
    })
    @GetMapping("/{boardId}")
    @PreAuthorize("@authorizationSecurityService.canViewBoard(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<KanbanBoardDTO>> getKanbanBoard(@PathVariable UUID boardId) {
        KanbanBoardDTO dto = kanbanBoardService.getBoard(boardId);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Kanban board retrieved successfully", dto));
    }

    // -------------------------------------------------------------------------
    // Lists
    // -------------------------------------------------------------------------

    @Operation(summary = "Create a kanban list", responses = {
            @ApiResponse(description = "Created",      responseCode = "201"),
            @ApiResponse(description = "Unauthorized", responseCode = "401"),
            @ApiResponse(description = "Forbidden",    responseCode = "403")
    })
    @PostMapping("/{boardId}/list")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<KanbanListDTO>> createKanbanList(
            @PathVariable UUID boardId,
            @Valid @RequestBody KanbanListCreationRequestDTO request) {
        KanbanListDTO dto = kanbanBoardService.createList(boardId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiSuccessResponseDTO<>(HttpStatus.CREATED.value(), Instant.now(), "Kanban list created successfully", dto));
    }

    @Operation(summary = "Update a kanban list", responses = {
            @ApiResponse(description = "Success",      responseCode = "200"),
            @ApiResponse(description = "Unauthorized", responseCode = "401"),
            @ApiResponse(description = "Forbidden",    responseCode = "403"),
            @ApiResponse(description = "Not found",    responseCode = "404")
    })
    @PatchMapping("/{boardId}/list/{listId}")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<KanbanListDTO>> updateKanbanList(
            @PathVariable UUID boardId,
            @PathVariable UUID listId,
            @Valid @RequestBody KanbanListUpdateRequestDTO request) {
        KanbanListDTO dto = kanbanBoardService.updateList(boardId, listId, request);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Kanban list updated successfully", dto));
    }

    @Operation(summary = "Delete a kanban list", responses = {
            @ApiResponse(description = "Success",      responseCode = "200"),
            @ApiResponse(description = "Unauthorized", responseCode = "401"),
            @ApiResponse(description = "Forbidden",    responseCode = "403"),
            @ApiResponse(description = "Not found",    responseCode = "404")
    })
    @DeleteMapping("/{boardId}/list/{listId}")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> deleteKanbanList(
            @PathVariable UUID boardId,
            @PathVariable UUID listId) {
        kanbanBoardService.deleteList(boardId, listId);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Kanban list deleted successfully"));
    }

    // -------------------------------------------------------------------------
    // Cards
    // -------------------------------------------------------------------------

    @Operation(summary = "Create a kanban card", responses = {
            @ApiResponse(description = "Created",      responseCode = "201"),
            @ApiResponse(description = "Unauthorized", responseCode = "401"),
            @ApiResponse(description = "Forbidden",    responseCode = "403")
    })
    @PostMapping("/{boardId}/card")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<KanbanCardDTO>> createKanbanCard(
            @PathVariable UUID boardId,
            @Valid @RequestBody KanbanCardCreationRequestDTO request) {
        KanbanCardDTO dto = kanbanBoardService.createCard(boardId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiSuccessResponseDTO<>(HttpStatus.CREATED.value(), Instant.now(), "Kanban card created successfully", dto));
    }

    @Operation(summary = "Get a kanban card by ID", responses = {
            @ApiResponse(description = "Success",      responseCode = "200"),
            @ApiResponse(description = "Unauthorized", responseCode = "401"),
            @ApiResponse(description = "Forbidden",    responseCode = "403"),
            @ApiResponse(description = "Not found",    responseCode = "404")
    })
    @GetMapping("/{boardId}/card/{cardId}")
    @PreAuthorize("@authorizationSecurityService.canViewBoard(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<KanbanCardDetailsDTO>> getKanbanCard(
            @PathVariable UUID boardId,
            @PathVariable UUID cardId) {
        KanbanCardDetailsDTO dto = kanbanBoardService.getCard(boardId, cardId);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Kanban card retrieved successfully", dto));
    }

    @Operation(summary = "Delete a kanban card", responses = {
            @ApiResponse(description = "Success",      responseCode = "200"),
            @ApiResponse(description = "Unauthorized", responseCode = "401"),
            @ApiResponse(description = "Forbidden",    responseCode = "403"),
            @ApiResponse(description = "Not found",    responseCode = "404")
    })
    @DeleteMapping("/{boardId}/card/{cardId}")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> deleteKanbanCard(
            @PathVariable UUID boardId,
            @PathVariable UUID cardId) {
        kanbanBoardService.deleteCard(boardId, cardId);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Kanban card deleted successfully"));
    }

    @Operation(summary = "Update a kanban card", responses = {
            @ApiResponse(description = "Success",      responseCode = "200"),
            @ApiResponse(description = "Unauthorized", responseCode = "401"),
            @ApiResponse(description = "Forbidden",    responseCode = "403"),
            @ApiResponse(description = "Not found",    responseCode = "404")
    })
    @PatchMapping("/{boardId}/card/{cardId}")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> updateKanbanCard(
            @PathVariable UUID boardId,
            @PathVariable UUID cardId,
            @Valid @RequestBody KanbanCardUpdateRequestDTO request) {
        kanbanBoardService.updateCard(boardId, cardId, request);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Kanban card updated successfully"));
    }

    // -------------------------------------------------------------------------
    // Checklists
    // -------------------------------------------------------------------------

    @Operation(summary = "Add a checklist to a card", responses = {
            @ApiResponse(description = "Created",      responseCode = "201"),
            @ApiResponse(description = "Unauthorized", responseCode = "401"),
            @ApiResponse(description = "Forbidden",    responseCode = "403")
    })
    @PostMapping("/{boardId}/card/{cardId}/checklists")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<ChecklistDTO>> addChecklist(
            @PathVariable UUID boardId,
            @PathVariable UUID cardId,
            @Valid @RequestBody ChecklistDTO request) {
        ChecklistDTO dto = kanbanBoardService.addChecklist(boardId, cardId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiSuccessResponseDTO<>(HttpStatus.CREATED.value(), Instant.now(), "Checklist created successfully", dto));
    }

    @Operation(summary = "Update a checklist on a card", responses = {
            @ApiResponse(description = "Success",      responseCode = "200"),
            @ApiResponse(description = "Unauthorized", responseCode = "401"),
            @ApiResponse(description = "Forbidden",    responseCode = "403"),
            @ApiResponse(description = "Not found",    responseCode = "404")
    })
    @PatchMapping("/{boardId}/card/{cardId}/checklists/{checklistId}")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> updateChecklist(
            @PathVariable UUID boardId,
            @PathVariable UUID cardId,
            @PathVariable UUID checklistId,
            @Valid @RequestBody ChecklistUpdateRequestDTO request) {
        kanbanBoardService.updateChecklist(boardId, cardId, checklistId, request);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Checklist updated successfully"));
    }

    @Operation(summary = "Delete a checklist from a card", responses = {
            @ApiResponse(description = "Success",      responseCode = "200"),
            @ApiResponse(description = "Unauthorized", responseCode = "401"),
            @ApiResponse(description = "Forbidden",    responseCode = "403"),
            @ApiResponse(description = "Not found",    responseCode = "404")
    })
    @DeleteMapping("/{boardId}/card/{cardId}/checklists/{checklistId}")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> deleteChecklist(
            @PathVariable UUID boardId,
            @PathVariable UUID cardId,
            @PathVariable UUID checklistId) {
        kanbanBoardService.deleteChecklist(boardId, cardId, checklistId);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Checklist deleted successfully"));
    }

    // -------------------------------------------------------------------------
    // Checklist items
    // -------------------------------------------------------------------------

    @Operation(summary = "Add an item to a checklist", responses = {
            @ApiResponse(description = "Created",      responseCode = "201"),
            @ApiResponse(description = "Unauthorized", responseCode = "401"),
            @ApiResponse(description = "Forbidden",    responseCode = "403")
    })
    @PostMapping("/{boardId}/card/{cardId}/checklists/{checklistId}/items")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<ChecklistItemDTO>> addChecklistItem(
            @PathVariable UUID boardId,
            @PathVariable UUID cardId,
            @PathVariable UUID checklistId,
            @Valid @RequestBody ChecklistItemCreationRequestDTO request) {
        ChecklistItemDTO dto = kanbanBoardService.addChecklistItem(boardId, cardId, checklistId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiSuccessResponseDTO<>(HttpStatus.CREATED.value(), Instant.now(), "Checklist item created successfully", dto));
    }

    @Operation(summary = "Update an item in a checklist", responses = {
            @ApiResponse(description = "Success",      responseCode = "200"),
            @ApiResponse(description = "Unauthorized", responseCode = "401"),
            @ApiResponse(description = "Forbidden",    responseCode = "403"),
            @ApiResponse(description = "Not found",    responseCode = "404")
    })
    @PatchMapping("/{boardId}/card/{cardId}/checklists/{checklistId}/items/{itemId}")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> updateChecklistItem(
            @PathVariable UUID boardId,
            @PathVariable UUID cardId,
            @PathVariable UUID checklistId,
            @PathVariable UUID itemId,
            @Valid @RequestBody ChecklistItemUpdateRequestDTO request) {
        kanbanBoardService.updateChecklistItem(boardId, cardId, checklistId, itemId, request);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Checklist item updated successfully"));
    }

    @Operation(summary = "Delete an item from a checklist", responses = {
            @ApiResponse(description = "Success",      responseCode = "200"),
            @ApiResponse(description = "Unauthorized", responseCode = "401"),
            @ApiResponse(description = "Forbidden",    responseCode = "403"),
            @ApiResponse(description = "Not found",    responseCode = "404")
    })
    @DeleteMapping("/{boardId}/card/{cardId}/checklists/{checklistId}/items/{itemId}")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> deleteChecklistItem(
            @PathVariable UUID boardId,
            @PathVariable UUID cardId,
            @PathVariable UUID checklistId,
            @PathVariable UUID itemId) {
        kanbanBoardService.deleteChecklistItem(boardId, cardId, checklistId, itemId);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Checklist item deleted successfully"));
    }

    // -------------------------------------------------------------------------
    // Comments
    // -------------------------------------------------------------------------

    @Operation(summary = "Add a comment to a card", responses = {
            @ApiResponse(description = "Created",      responseCode = "201"),
            @ApiResponse(description = "Unauthorized", responseCode = "401"),
            @ApiResponse(description = "Forbidden",    responseCode = "403")
    })
    @PostMapping("/{boardId}/card/{cardId}/comments")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<CardCommentDTO>> addComment(
            @PathVariable UUID boardId,
            @PathVariable UUID cardId,
            @Valid @RequestBody CommentCreationRequestDTO request,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        CardCommentDTO dto = kanbanBoardService.addComment(boardId, cardId, request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiSuccessResponseDTO<>(HttpStatus.CREATED.value(), Instant.now(), "Comment created successfully", dto));
    }

    @Operation(summary = "Update a comment on a card", responses = {
            @ApiResponse(description = "Success",      responseCode = "200"),
            @ApiResponse(description = "Unauthorized", responseCode = "401"),
            @ApiResponse(description = "Forbidden",    responseCode = "403"),
            @ApiResponse(description = "Not found",    responseCode = "404")
    })
    @PatchMapping("/{boardId}/card/{cardId}/comments/{commentId}")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> updateComment(
            @PathVariable UUID boardId,
            @PathVariable UUID cardId,
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentUpdateRequestDTO request,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        kanbanBoardService.updateComment(boardId, cardId, commentId, request, userDetails.getUserId());
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Comment updated successfully"));
    }

    @Operation(summary = "Delete a comment from a card", responses = {
            @ApiResponse(description = "Success",      responseCode = "200"),
            @ApiResponse(description = "Unauthorized", responseCode = "401"),
            @ApiResponse(description = "Forbidden",    responseCode = "403"),
            @ApiResponse(description = "Not found",    responseCode = "404")
    })
    @DeleteMapping("/{boardId}/card/{cardId}/comments/{commentId}")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> deleteComment(
            @PathVariable UUID boardId,
            @PathVariable UUID cardId,
            @PathVariable UUID commentId,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        kanbanBoardService.deleteComment(boardId, cardId, commentId, userDetails.getUserId());
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Comment deleted successfully"));
    }
}