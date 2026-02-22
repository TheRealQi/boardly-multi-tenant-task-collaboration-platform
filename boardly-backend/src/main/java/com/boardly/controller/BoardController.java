package com.boardly.controller;

import com.boardly.common.dto.ApiSuccessResponseDTO;
import com.boardly.common.dto.board.BoardChangeVisibilityRequestDTO;
import com.boardly.common.dto.board.BoardCreationRequestDTO;
import com.boardly.common.dto.board.BoardDTO;
import com.boardly.common.dto.board.BoardEditRequestDTO;
import com.boardly.security.model.AppUserDetails;
import com.boardly.service.BoardService;
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
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping("/")
    @PreAuthorize("@authorizationSecurityService.canCreateBoard(#boardCreationRequestDTO.workspaceId, #boardCreationRequestDTO.boardVisibility)")
    public ResponseEntity<ApiSuccessResponseDTO<BoardDTO>> createBoard(@RequestBody @Valid BoardCreationRequestDTO boardCreationRequestDTO, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        BoardDTO createdBoard = boardService.createBoard(boardCreationRequestDTO, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Board created successfully", createdBoard));
    }

    @GetMapping("/{boardId}")
    @PreAuthorize("@authorizationSecurityService.canViewBoard(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<BoardDTO>> getBoard(@PathVariable UUID boardId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        BoardDTO boardDTO = boardService.getBoard(boardId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Board retrieved successfully", boardDTO));
    }

    @DeleteMapping("/{boardId}")
    @PreAuthorize("@authorizationSecurityService.canDeleteBoard(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> deleteBoard(@PathVariable UUID boardId) {
        boardService.deleteBoard(boardId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Board deleted successfully", null));
    }

    @PutMapping("/{boardId}")
    @PreAuthorize("@authorizationSecurityService.canEditBoard(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> editBoard(@PathVariable UUID boardId, @RequestBody @Valid BoardEditRequestDTO boardEditRequestDTO, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        boardService.editBoard(boardId, boardEditRequestDTO, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Board updated successfully"));
    }

    @PutMapping("/{boardId}/visibility")
    @PreAuthorize("@authorizationSecurityService.canChangeBoardVisibility(#boardId, #boardChangeVisibilityRequestDTO.boardVisibility)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> changeBoardVisibility(@PathVariable UUID boardId, @RequestBody @Valid BoardChangeVisibilityRequestDTO boardChangeVisibilityRequestDTO, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        boardService.changeBoardVisibility(boardId, boardChangeVisibilityRequestDTO, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Board visibility changed successfully"));
    }

    @GetMapping("/workspace/{workspaceId}")
    @PreAuthorize("@authorizationSecurityService.isWorkspaceMember(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<List<BoardDTO>>> getBoardsByWorkspace(@PathVariable UUID workspaceId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        List<BoardDTO> boards = boardService.getBoardsForWorkspace(workspaceId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Boards retrieved successfully", boards));
    }

    @GetMapping("/")
    public ResponseEntity<ApiSuccessResponseDTO<List<BoardDTO>>> getBoardsForUser(@AuthenticationPrincipal AppUserDetails appUserDetails) {
        List<BoardDTO> boards = boardService.getBoardsForUser(appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Boards retrieved successfully", boards));
    }
}
