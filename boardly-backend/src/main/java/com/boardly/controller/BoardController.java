package com.boardly.controller;

import com.boardly.commmon.dto.ApiSuccessResponseDTO;
import com.boardly.commmon.dto.board.BoardCreationDTO;
import com.boardly.commmon.dto.board.BoardDTO;
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
}
