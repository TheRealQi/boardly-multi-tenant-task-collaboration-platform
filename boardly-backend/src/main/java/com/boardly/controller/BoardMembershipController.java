package com.boardly.controller;

import com.boardly.common.dto.ApiSuccessResponseDTO;
import com.boardly.common.dto.board.*;
import com.boardly.security.model.AppUserDetails;
import com.boardly.service.BoardMembershipService;
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
@Tag(name = "Board Membership")
public class BoardMembershipController {

    private final BoardMembershipService boardMembershipService;

    public BoardMembershipController(BoardMembershipService boardMembershipService) {
        this.boardMembershipService = boardMembershipService;
    }

    @Operation(
            description = "Join board endpoint",
            summary = "Join a board",
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
    @PostMapping("/{boardId}/join")
    @PreAuthorize("@authorizationSecurityService.canJoinBoard(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> joinBoard(@PathVariable UUID boardId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        boardMembershipService.joinBoard(boardId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Joined board successfully", null));
    }

    @Operation(
            description = "Leave board endpoint",
            summary = "Leave a board",
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
    @DeleteMapping("/{boardId}/leave")
    @PreAuthorize("@authorizationSecurityService.canLeaveBoard(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> leaveBoard(@PathVariable UUID boardId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        boardMembershipService.leaveBoard(boardId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Left board successfully", null));
    }

    @Operation(
            description = "Get board members endpoint",
            summary = "Get all members of a board",
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
    @GetMapping("/{boardId}/members")
    @PreAuthorize("@authorizationSecurityService.isBoardMember(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<List<BoardMemberDTO>>> getBoardMembers(@PathVariable UUID boardId) {
        List<BoardMemberDTO> boardMembers = boardMembershipService.getBoardMembers(boardId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Board members retrieved successfully", boardMembers));
    }

    @Operation(
            description = "Remove member from board endpoint",
            summary = "Remove a member from a board",
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
    @DeleteMapping("/{boardId}/members/{memberId}")
    @PreAuthorize("@authorizationSecurityService.canManageBoardMembers(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> removeMemberFromBoard(@PathVariable UUID boardId, @PathVariable UUID memberId) {
        boardMembershipService.removeBoardMember(boardId, memberId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Removed member from board successfully", null));
    }

    @Operation(
            description = "Change member role in board endpoint",
            summary = "Change a member's role in a board",
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
    @PutMapping("/{boardId}/members/{memberId}/change-role")
    @PreAuthorize("@authorizationSecurityService.canManageBoardMembers(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> changeMemberRoleInBoard(@PathVariable UUID boardId, @PathVariable UUID memberId, @Valid @RequestBody BoardChangeRoleRequestDTO boardChangeRoleRequestDTO) {
        boardMembershipService.changeBoardMemberRole(boardId, memberId, boardChangeRoleRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Changed member role successfully", null));
    }

    @Operation(
            description = "Add member to board endpoint",
            summary = "Add a member to a board",
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
    @PutMapping("/{boardId}/members/add-member")
    @PreAuthorize("@authorizationSecurityService.canManageBoardMembers(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<BoardMemberDTO>> addMemberToBoard(@PathVariable UUID boardId, @Valid @RequestBody BoardAddWSMemberRequestDTO boardAddWSMemberRequestDTO) {
        BoardMemberDTO newMember = boardMembershipService.addWorkspaceMemberToBoard(boardId, boardAddWSMemberRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Added workspace member successfully", newMember));
    }

    @Operation(
            description = "Invite member to board endpoint",
            summary = "Invite a member to a board",
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
    @PostMapping("/{boardId}/members/invite")
    @PreAuthorize("@authorizationSecurityService.canManageBoardMembers(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<BoardInviteDTO>> inviteMemberToBoard(@PathVariable UUID boardId, @Valid @RequestBody BoardInviteRequestDTO boardInviteRequestDTO, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        BoardInviteDTO invite = boardMembershipService.inviteToBoard(boardId, boardInviteRequestDTO, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Invited member to board successfully", invite));
    }

    @Operation(
            description = "Accept board invite endpoint",
            summary = "Accept a board invitation",
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
    @PutMapping("/invites/{inviteId}/accept")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> acceptBoardInvite(@PathVariable UUID inviteId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        boardMembershipService.acceptInvitationToBoard(inviteId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Accepted board invitation successfully", null));
    }

    @Operation(
            description = "Decline board invite endpoint",
            summary = "Decline a board invitation",
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
    @PutMapping("/invites/{inviteId}/decline")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> declineBoardInvite(@PathVariable UUID inviteId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        boardMembershipService.declineInvitationToBoard(inviteId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Declined board invitation successfully", null));
    }

    @Operation(
            description = "Cancel board invite endpoint",
            summary = "Cancel a board invitation",
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
    @PutMapping("/{boardId}/invites/{inviteId}/cancel")
    @PreAuthorize("@authorizationSecurityService.canManageBoardMembers(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> cancelBoardInvite(@PathVariable UUID boardId, @PathVariable UUID inviteId) {
        boardMembershipService.cancelBoardInvitation(inviteId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Cancelled board invitation successfully", null));
    }

    @Operation(
            description = "Get board invites endpoint",
            summary = "Get all pending invites for a board",
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
    @GetMapping("/{boardId}/invites")
    @PreAuthorize("@authorizationSecurityService.canManageBoardMembers(#boardId)")
    public ResponseEntity<ApiSuccessResponseDTO<List<BoardInviteDTO>>> getBoardInvites(@PathVariable UUID boardId) {
        List<BoardInviteDTO> boardInvites = boardMembershipService.getBoardPendingInvites(boardId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Board pending invitations retrieved successfully.", boardInvites));
    }

    @Operation(
            description = "Get user board invites endpoint",
            summary = "Get all pending board invites for a user",
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
    @GetMapping("/invites")
    public ResponseEntity<ApiSuccessResponseDTO<List<BoardInviteDTO>>> getUserBoardInvites(@AuthenticationPrincipal AppUserDetails appUserDetails) {
        List<BoardInviteDTO> boardInvites = boardMembershipService.getUserBoardPendingInvites(appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "User board pending invitations retrieved successfully.", boardInvites));
    }
}
