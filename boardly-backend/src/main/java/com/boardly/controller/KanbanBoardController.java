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

    @Operation(
            description = "Get kanban board endpoint",
            summary = "Get a kanban board by ID",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @GetMapping("/{boardId}")
    @PreAuthorize("@authorizationSecurityService.canViewBoard(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<KanbanBoardDTO>> getKanbanBoard(@PathVariable("boardId") UUID boardId) {
        KanbanBoardDTO kanbanBoardDTO = kanbanBoardService.getBoard(boardId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Kanban Board retrieved successfully", kanbanBoardDTO));
    }

    @Operation(
            description = "Create kanban list endpoint",
            summary = "Create a kanban list",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "201"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PostMapping("/{boardId}/list")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<KanbanListDTO>> createKanbanList(@PathVariable("boardId") UUID boardId, @Valid @RequestBody KanbanListCreationRequestDTO kanbanListCreationRequestDTO) {
        KanbanListDTO kanbanListDTO = kanbanBoardService.createList(boardId, kanbanListCreationRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponseDTO<>(HttpStatus.CREATED.value(), Instant.now(), "Kanban List created successfully", kanbanListDTO));
    }

    @Operation(
            description = "Update kanban list endpoint",
            summary = "Update a kanban list",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PutMapping("/{boardId}/list/{listId}")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<KanbanListDTO>> updateKanbanList(@PathVariable("boardId") UUID boardId, @PathVariable("listId") UUID listId, @Valid @RequestBody KanbanListUpdateRequestDTO kanbanListUpdateRequestDTO) {
        KanbanListDTO kanbanListDTO = kanbanBoardService.updateList(boardId, listId, kanbanListUpdateRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Kanban List updated successfully", kanbanListDTO));
    }

    @Operation(
            description = "Delete kanban list endpoint",
            summary = "Delete a kanban list",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @DeleteMapping("/{boardId}/list/{listId}")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> deleteKanbanList(@PathVariable("boardId") UUID boardId, @PathVariable("listId") UUID listId) {
        kanbanBoardService.deleteList(boardId, listId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Kanban List deleted successfully"));
    }

    @Operation(
            description = "Create kanban card endpoint",
            summary = "Create a kanban card",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "201"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PostMapping("/{boardId}/card")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<KanbanCardDTO>> createKanbanCard(@PathVariable("boardId") UUID boardId, @Valid @RequestBody KanbanCardCreationRequestDTO kanbanCardCreationRequestDTO) {
        KanbanCardDTO kanbanCardDTO = kanbanBoardService.createCard(boardId, kanbanCardCreationRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponseDTO<>(HttpStatus.CREATED.value(), Instant.now(), "Kanban Card created successfully", kanbanCardDTO));
    }

    @Operation(
            description = "Get kanban card endpoint",
            summary = "Get a kanban card by ID",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @GetMapping("/{boardId}/card/{cardId}")
    @PreAuthorize("@authorizationSecurityService.canViewBoard(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<KanbanCardDetailsDTO>> getKanbanCard(@PathVariable("boardId") UUID boardId, @PathVariable("cardId") UUID cardId) {
        KanbanCardDetailsDTO kanbanCardDetailsDTO = kanbanBoardService.getCard(boardId, cardId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Kanban Card retrieved successfully", kanbanCardDetailsDTO));
    }

    @Operation(
            description = "Update kanban card endpoint",
            summary = "Update a kanban card",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PutMapping("/{boardId}/card/{cardId}")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> updateKanbanCard(@PathVariable("boardId") UUID boardId, @PathVariable("cardId") UUID cardId, @Valid @RequestBody KanbanCardUpdateRequestDTO kanbanCardUpdateRequestDTO) {
        kanbanBoardService.updateCard(boardId, cardId, kanbanCardUpdateRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Kanban Card updated successfully"));
    }

    @Operation(
            description = "Add checklist endpoint",
            summary = "Add a checklist to a card",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "201"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PostMapping("/{boardId}/card/{cardId}/checklists")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<ChecklistDTO>> addChecklist(@PathVariable("boardId") UUID boardId, @PathVariable("cardId") UUID cardId, @Valid @RequestBody ChecklistDTO checklistDTO) {
        ChecklistDTO checklist = kanbanBoardService.addChecklist(boardId, cardId, checklistDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponseDTO<>(HttpStatus.CREATED.value(), Instant.now(), "Checklist created successfully", checklist));
    }

    @Operation(
            description = "Delete checklist endpoint",
            summary = "Delete a checklist from a card",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @DeleteMapping("/{boardId}/card/{cardId}/checklists/{checklistId}")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> deleteChecklist(@PathVariable("boardId") UUID boardId, @PathVariable("cardId") UUID cardId, @PathVariable("checklistId") UUID checklistId) {
        kanbanBoardService.deleteChecklist(boardId, cardId, checklistId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Checklist deleted successfully"));
    }

    @Operation(
            description = "Update checklist endpoint",
            summary = "Update a checklist on a card",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PutMapping("/{boardId}/card/{cardId}/checklists/{checklistId}")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> updateChecklist(@PathVariable("boardId") UUID boardId, @PathVariable("cardId") UUID cardId, @PathVariable("checklistId") UUID checklistId, @Valid @RequestBody ChecklistUpdateRequestDTO checklistUpdateRequestDTO) {
        kanbanBoardService.updateChecklist(boardId, cardId, checklistId, checklistUpdateRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Checklist updated successfully"));
    }

    @Operation(
            description = "Add checklist item endpoint",
            summary = "Add an item to a checklist",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "201"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PostMapping("/{boardId}/card/{cardId}/checklists/{checklistId}/items")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<ChecklistItemDTO>> addChecklistItem(@PathVariable("boardId") UUID boardId, @PathVariable("cardId") UUID cardId, @PathVariable("checklistId") UUID checklistId, @Valid @RequestBody ChecklistItemCreationRequestDTO checklistItemCreationRequestDTO) {
        ChecklistItemDTO checklistItemDTO = kanbanBoardService.addChecklistItem(boardId, cardId, checklistId, checklistItemCreationRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponseDTO<>(HttpStatus.CREATED.value(), Instant.now(), "Checklist item created successfully", checklistItemDTO));
    }

    @Operation(
            description = "Delete checklist item endpoint",
            summary = "Delete an item from a checklist",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @DeleteMapping("/{boardId}/card/{cardId}/checklists/{checklistId}/items/{itemId}")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> deleteChecklistItem(@PathVariable("boardId") UUID boardId, @PathVariable("cardId") UUID cardId, @PathVariable("checklistId") UUID checklistId, @PathVariable("itemId") UUID itemId) {
        kanbanBoardService.deleteChecklistItem(boardId, cardId, checklistId, itemId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Checklist item deleted successfully"));
    }

    @Operation(
            description = "Update checklist item endpoint",
            summary = "Update an item in a checklist",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PutMapping("/{boardId}/card/{cardId}/checklists/{checklistId}/items/{itemId}")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> updateChecklistItem(@PathVariable("boardId") UUID boardId, @PathVariable("cardId") UUID cardId, @PathVariable("checklistId") UUID checklistId, @PathVariable("itemId") UUID itemId, @Valid @RequestBody ChecklistItemUpdateRequestDTO checklistItemUpdateRequestDTO) {
        kanbanBoardService.updateChecklistItem(boardId, cardId, checklistId, itemId, checklistItemUpdateRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Checklist item updated successfully"));
    }

    @Operation(
            description = "Add comment endpoint",
            summary = "Add a comment to a card",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "201"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PostMapping("/{boardId}/card/{cardId}/comments")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<CardCommentDTO>> addComment(@PathVariable("boardId") UUID boardId, @PathVariable("cardId") UUID cardId, @Valid @RequestBody CommentCreationRequestDTO commentCreationRequestDTO, @AuthenticationPrincipal AppUserDetails userDetails) {
        CardCommentDTO cardCommentDTO = kanbanBoardService.addComment(boardId, cardId, commentCreationRequestDTO, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponseDTO<>(HttpStatus.CREATED.value(), Instant.now(), "Comment created successfully", cardCommentDTO));
    }

    @Operation(
            description = "Delete comment endpoint",
            summary = "Delete a comment from a card",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @DeleteMapping("/{boardId}/card/{cardId}/comments/{commentId}")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> deleteComment(@PathVariable("boardId") UUID boardId, @PathVariable("cardId") UUID cardId, @PathVariable("commentId") UUID commentId) {
        kanbanBoardService.deleteComment(boardId, cardId, commentId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Comment deleted successfully"));
    }

    @Operation(
            description = "Update comment endpoint",
            summary = "Update a comment on a card",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PutMapping("/{boardId}/card/{cardId}/comments/{commentId}")
    @PreAuthorize("@authorizationSecurityService.canEditBoardContent(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> updateComment(@PathVariable("boardId") UUID boardId, @PathVariable("cardId") UUID cardId, @PathVariable("commentId") UUID commentId, @Valid @RequestBody CommentUpdateRequestDTO commentUpdateRequestDTO) {
        kanbanBoardService.updateComment(boardId, cardId, commentId, commentUpdateRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Comment updated successfully"));
    }
}
