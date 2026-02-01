package com.boardly.controller;

import com.boardly.commmon.dto.ApiSuccessResponseDTO;
import com.boardly.commmon.dto.board.BoardChangeRoleDTO;
import com.boardly.commmon.dto.board.BoardDTO;
import com.boardly.security.model.AppUserDetails;
import com.boardly.service.BoardMembershipService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
        return null;
    }

    @PutMapping("/{boardId}/leave")
    @PreAuthorize("@authorizationSecurityService.canLeaveBoard(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> leaveBoard(@PathVariable UUID boardId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        boardMembershipService.leaveBoard(boardId, appUserDetails);
        return null;
    }

    @GetMapping("/{boardId}/members")
    @PreAuthorize("@authorizationSecurityService.isBoardMember(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<BoardDTO>> getBoardMembers(@PathVariable UUID boardId) {
        boardMembershipService.getBoardMembers(boardId);
        return null;
    }

    @PutMapping("/{boardId}/members/{memberId}/remove")
    @PreAuthorize("@authorizationSecurityService.canManageBoardMembers(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> removeMemberFromBoard(@PathVariable UUID boardId, @PathVariable UUID memberId) {
        boardMembershipService.removeBoardMember(boardId, memberId);
        return null;
    }

    @PutMapping("/{boardId}/members/{memberId}/change-role")
    @PreAuthorize("@authorizationSecurityService.canManageBoardMembers(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> changeMemberRoleInBoard(@PathVariable UUID boardId, @PathVariable UUID memberId, @RequestParam BoardChangeRoleDTO boardChangeRoleDTO) {
        boardMembershipService.changeBoardMemberRole(boardId, memberId, boardChangeRoleDTO);
        return null;
    }

    @PutMapping("/{boardId}/members/add-member/{memberId}")
    @PreAuthorize("@authorizationSecurityService.canManageBoardMembers(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> addMemberToBoard(@PathVariable UUID boardId, @RequestParam UUID memberId) {
        boardMembershipService.addWorkspaceMemberToBoard(boardId, memberId);
        return null;
    }

    @PostMapping("/{boardId}/members/invite")
    @PreAuthorize("@authorizationSecurityService.canManageBoardMembers(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> inviteMemberToBoard(@PathVariable UUID boardId, @RequestParam String email, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        boardMembershipService.inviteToBoard(boardId, email, appUserDetails);
        return null;
    }

    @PutMapping("/invites/{inviteId}/accept")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> acceptBoardInvite(@PathVariable UUID inviteId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        boardMembershipService.acceptInvitationToBoard(inviteId, appUserDetails);
        return null;
    }

    @PutMapping("/invites/{inviteId}/decline")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> declineBoardInvite(@PathVariable UUID inviteId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        boardMembershipService.declineInvitationToBoard(inviteId, appUserDetails);
        return null;
    }

    @GetMapping("/{boardId}/invites")
    @PreAuthorize("@authorizationSecurityService.canManageBoardMembers(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<BoardDTO>> getBoardInvites(@PathVariable UUID boardId) {
        boardMembershipService.getBoardPendingInvites(boardId);
        return null;
    }

    @GetMapping("/invites")
    public ResponseEntity<ApiSuccessResponseDTO<BoardDTO>> getUserBoardInvites(@AuthenticationPrincipal AppUserDetails appUserDetails) {
        boardMembershipService.getUserBoardPendingInvites(appUserDetails);
        return null;
    }
}
