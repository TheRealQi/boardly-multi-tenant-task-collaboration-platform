package com.boardly.controller;

import com.boardly.commmon.dto.ApiSuccessResponseDTO;
import com.boardly.commmon.dto.board.BoardChangeVisibilityDTO;
import com.boardly.commmon.dto.board.BoardCreationDTO;
import com.boardly.commmon.dto.board.BoardDTO;
import com.boardly.commmon.dto.board.BoardEditDTO;
import com.boardly.security.model.AppUserDetails;
import com.boardly.service.BoardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("${api.base-path}/${api.version}/board")
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping("/")
    @PreAuthorize("@authorizationSecurityService.canCreateBoard(#boardCreationDTO.workspaceId, #boardCreationDTO.boardVisibility)")
    public ResponseEntity<ApiSuccessResponseDTO<BoardDTO>> createBoard(@RequestBody BoardCreationDTO boardCreationDTO, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        BoardDTO createdBoard = boardService.createBoard(boardCreationDTO, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Board created successfully", createdBoard));
    }

    @GetMapping("/{boardId}")
    @PreAuthorize("@authorizationSecurityService.canAccessBoard(#boardId)")
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
    @PreAuthorize("@authorizationSecurityService.canEditBoardSettings(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<BoardDTO>> editBoard(@PathVariable UUID boardId, @RequestBody BoardEditDTO boardEditDTO, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        BoardDTO updatedBoard = boardService.editBoard(boardId, boardEditDTO, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Board updated successfully", updatedBoard));
    }

    @PutMapping("/{boardId}/visibility")
    @PreAuthorize("@authorizationSecurityService.canChangeBoardVisibility(#boardId, #boardChangeVisibilityDTO.boardVisibility)")
    public ResponseEntity<ApiSuccessResponseDTO<BoardDTO>> changeBoardVisibility(@PathVariable UUID boardId, @RequestBody BoardChangeVisibilityDTO boardChangeVisibilityDTO, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        BoardDTO updatedBoard = boardService.changeBoardVisibility(boardId, boardChangeVisibilityDTO, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Board visibility changed successfully", updatedBoard));
    }
}
