package com.boardly.controller;

import com.boardly.common.dto.ApiSuccessResponseDTO;
import com.boardly.common.dto.board.BoardChangeVisibilityRequestDTO;
import com.boardly.common.dto.board.BoardCreationRequestDTO;
import com.boardly.common.dto.board.BoardDTO;
import com.boardly.common.dto.board.BoardEditRequestDTO;
import com.boardly.security.model.AppUserDetails;
import com.boardly.service.BoardService;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.base-path}/${api.version}/board")
@Tag(name = "Board")
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @Operation(
            description = "Create board endpoint",
            summary = "Create a new board",
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
    @PostMapping("/")
    @PreAuthorize("@authorizationSecurityService.canCreateBoard(#boardCreationRequestDTO.workspaceId, #boardCreationRequestDTO.boardVisibility)")
    public ResponseEntity<ApiSuccessResponseDTO<BoardDTO>> createBoard(@RequestBody @Valid BoardCreationRequestDTO boardCreationRequestDTO, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        BoardDTO createdBoard = boardService.createBoard(boardCreationRequestDTO, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Board created successfully", createdBoard));
    }

    @Operation(
            description = "Get board endpoint",
            summary = "Get a board by ID",
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
    public ResponseEntity<ApiSuccessResponseDTO<BoardDTO>> getBoard(@PathVariable UUID boardId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        BoardDTO boardDTO = boardService.getBoard(boardId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Board retrieved successfully", boardDTO));
    }

    @Operation(
            description = "Delete board endpoint",
            summary = "Delete a board by ID",
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
    @DeleteMapping("/{boardId}")
    @PreAuthorize("@authorizationSecurityService.canDeleteBoard(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> deleteBoard(@PathVariable UUID boardId) {
        boardService.deleteBoard(boardId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Board deleted successfully", null));
    }

    @Operation(
            description = "Edit board endpoint",
            summary = "Edit a board by ID",
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
    @PutMapping("/{boardId}")
    @PreAuthorize("@authorizationSecurityService.canEditBoard(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> editBoard(@PathVariable UUID boardId, @RequestBody @Valid BoardEditRequestDTO boardEditRequestDTO, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        boardService.editBoard(boardId, boardEditRequestDTO, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Board updated successfully"));
    }

    @Operation(
            description = "Change board visibility endpoint",
            summary = "Change a board's visibility",
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
    @PutMapping("/{boardId}/visibility")
    @PreAuthorize("@authorizationSecurityService.canChangeBoardVisibility(#boardId, #boardChangeVisibilityRequestDTO.boardVisibility)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> changeBoardVisibility(@PathVariable UUID boardId, @RequestBody @Valid BoardChangeVisibilityRequestDTO boardChangeVisibilityRequestDTO, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        boardService.changeBoardVisibility(boardId, boardChangeVisibilityRequestDTO, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Board visibility changed successfully"));
    }

    @Operation(
            description = "Get boards by workspace endpoint",
            summary = "Get all boards in a workspace",
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
    @GetMapping("/workspace/{workspaceId}")
    @PreAuthorize("@authorizationSecurityService.isWorkspaceMember(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<List<BoardDTO>>> getBoardsByWorkspace(@PathVariable UUID workspaceId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        List<BoardDTO> boards = boardService.getBoardsForWorkspace(workspaceId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Boards retrieved successfully", boards));
    }

    @Operation(
            description = "Get boards for user endpoint",
            summary = "Get all boards for a user",
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
    @GetMapping("/")
    public ResponseEntity<ApiSuccessResponseDTO<List<BoardDTO>>> getBoardsForUser(@AuthenticationPrincipal AppUserDetails appUserDetails) {
        List<BoardDTO> boards = boardService.getBoardsForUser(appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Boards retrieved successfully", boards));
    }
}
