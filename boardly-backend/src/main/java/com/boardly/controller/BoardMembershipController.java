package com.boardly.controller;

import com.boardly.commmon.dto.ApiSuccessResponseDTO;
import com.boardly.commmon.dto.board.*;
import com.boardly.security.model.AppUserDetails;
import com.boardly.service.BoardMembershipService;
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
public class BoardMembershipController {

    private final BoardMembershipService boardMembershipService;

    public BoardMembershipController(BoardMembershipService boardMembershipService) {
        this.boardMembershipService = boardMembershipService;
    }

    @PostMapping("/{boardId}/join")
    @PreAuthorize("@authorizationSecurityService.canJoinBoard(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> joinBoard(@PathVariable UUID boardId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        boardMembershipService.joinBoard(boardId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Joined board successfully", null));
    }

    @DeleteMapping("/{boardId}/leave")
    @PreAuthorize("@authorizationSecurityService.canLeaveBoard(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> leaveBoard(@PathVariable UUID boardId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        boardMembershipService.leaveBoard(boardId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Left board successfully", null));
    }

    @GetMapping("/{boardId}/members")
    @PreAuthorize("@authorizationSecurityService.isBoardMember(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<List<BoardMemberDTO>>> getBoardMembers(@PathVariable UUID boardId) {
        List<BoardMemberDTO> boardMembers = boardMembershipService.getBoardMembers(boardId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Board members retrieved successfully", boardMembers));
    }

    @DeleteMapping("/{boardId}/members/{memberId}")
    @PreAuthorize("@authorizationSecurityService.canManageBoardMembers(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> removeMemberFromBoard(@PathVariable UUID boardId, @PathVariable UUID memberId) {
        boardMembershipService.removeBoardMember(boardId, memberId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Removed member from board successfully", null));
    }

    @PutMapping("/{boardId}/members/{memberId}/change-role")
    @PreAuthorize("@authorizationSecurityService.canManageBoardMembers(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> changeMemberRoleInBoard(@PathVariable UUID boardId, @PathVariable UUID memberId, @Valid @RequestBody BoardChangeRoleRequestDTO boardChangeRoleRequestDTO) {
        boardMembershipService.changeBoardMemberRole(boardId, memberId, boardChangeRoleRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Changed member role successfully", null));
    }

    @PutMapping("/{boardId}/members/add-member")
    @PreAuthorize("@authorizationSecurityService.canManageBoardMembers(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<BoardMemberDTO>> addMemberToBoard(@PathVariable UUID boardId, @Valid @RequestBody BoardAddWSMemberRequestDTO boardAddWSMemberRequestDTO) {
        BoardMemberDTO newMember = boardMembershipService.addWorkspaceMemberToBoard(boardId, boardAddWSMemberRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Added workspace member successfully", newMember));
    }

    @PostMapping("/{boardId}/members/invite")
    @PreAuthorize("@authorizationSecurityService.canManageBoardMembers(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<BoardInviteDTO>> inviteMemberToBoard(@PathVariable UUID boardId, @Valid @RequestBody BoardInviteRequestDTO boardInviteRequestDTO, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        BoardInviteDTO invite = boardMembershipService.inviteToBoard(boardId, boardInviteRequestDTO, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Invited member to board successfully", invite));
    }

    @PutMapping("/invites/{inviteId}/accept")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> acceptBoardInvite(@PathVariable UUID inviteId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        boardMembershipService.acceptInvitationToBoard(inviteId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Accepted board invitation successfully", null));
    }

    @PutMapping("/invites/{inviteId}/decline")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> declineBoardInvite(@PathVariable UUID inviteId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        boardMembershipService.declineInvitationToBoard(inviteId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Declined board invitation successfully", null));
    }

    @PutMapping("/{boardId}/invites/{inviteId}/cancel")
    @PreAuthorize("@authorizationSecurityService.canManageBoardMembers(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> cancelBoardInvite(@PathVariable UUID boardId, @PathVariable UUID inviteId) {
        boardMembershipService.cancelBoardInvitation(inviteId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Cancelled board invitation successfully", null));
    }

    @GetMapping("/{boardId}/invites")
    @PreAuthorize("@authorizationSecurityService.canManageBoardMembers(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<List<BoardInviteDTO>>> getBoardInvites(@PathVariable UUID boardId) {
        List<BoardInviteDTO> boardInvites = boardMembershipService.getBoardPendingInvites(boardId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Board pending invitations retrieved successfully.", boardInvites));
    }

    @GetMapping("/invites")
    public ResponseEntity<ApiSuccessResponseDTO<List<BoardInviteDTO>>> getUserBoardInvites(@AuthenticationPrincipal AppUserDetails appUserDetails) {
        List<BoardInviteDTO> boardInvites = boardMembershipService.getUserBoardPendingInvites(appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "User board pending invitations retrieved successfully.", boardInvites));
    }
}