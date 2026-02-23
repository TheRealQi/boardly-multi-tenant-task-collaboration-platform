package com.boardly.controller;

import com.boardly.common.dto.ApiSuccessResponseDTO;
import com.boardly.common.dto.workspace.*;
import com.boardly.security.model.AppUserDetails;
import com.boardly.service.WorkspaceMembershipService;
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
@RequestMapping("${api.base-path}/${api.version}/workspace")
@Tag(name = "Workspace Membership")
public class WorkspaceMembershipController {
    private final WorkspaceMembershipService workspaceMembershipService;

    public WorkspaceMembershipController(WorkspaceMembershipService workspaceMembershipService) {
        this.workspaceMembershipService = workspaceMembershipService;
    }

    @Operation(
            description = "Get workspace members endpoint",
            summary = "Get all members of a workspace",
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
    @GetMapping("/{workspaceId}/members") // Done
    @PreAuthorize("@authorizationSecurityService.isWorkspaceMember(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<List<WorkspaceMemberDTO>>> getWorkspaceMembers(@PathVariable UUID workspaceId) {
        List<WorkspaceMemberDTO> members = workspaceMembershipService.getWorkspaceMembers(workspaceId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace members retrieved successfully", members));
    }

    @Operation(
            description = "Leave workspace endpoint",
            summary = "Leave a workspace",
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
    @DeleteMapping("/{workspaceId}/leave") // Done
    @PreAuthorize("@authorizationSecurityService.isWorkspaceMember(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> leaveWorkspace(@PathVariable UUID workspaceId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        workspaceMembershipService.leaveWorkspace(workspaceId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Left workspace successfully", null));
    }

    @Operation(
            description = "Invite member to workspace endpoint",
            summary = "Invite a member to a workspace",
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
    @PostMapping("/{workspaceId}/invite") // Done
    @PreAuthorize("@authorizationSecurityService.canInviteWorkspaceMembers(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> inviteMemberToWorkspace(@PathVariable UUID workspaceId, @RequestParam String email, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        workspaceMembershipService.inviteMemberToWorkspace(workspaceId, email, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Invitation sent successfully", null));
    }

    @Operation(
            description = "Remove member from workspace endpoint",
            summary = "Remove a member from a workspace",
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
    @DeleteMapping("/{workspaceId}/members/{memberId}") // Done
    @PreAuthorize("@authorizationSecurityService.canRemoveWorkspaceMembers(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> removeMemberFromWorkspace(@PathVariable UUID workspaceId, @PathVariable UUID memberId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        workspaceMembershipService.removeMemberFromWorkspace(workspaceId, memberId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Member removed successfully", null));
    }

    @Operation(
            description = "Accept workspace invite endpoint",
            summary = "Accept a workspace invitation",
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
    @PutMapping("/invite/{inviteId}/accept")
    public ResponseEntity<ApiSuccessResponseDTO<WorkspaceDTO>> acceptWorkspaceInvite( @PathVariable UUID inviteId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        WorkspaceDTO workspaceDTO = workspaceMembershipService.acceptInvitationToWorkspace(inviteId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace invite accepted successfully", workspaceDTO));
    }

    @Operation(
            description = "Decline workspace invite endpoint",
            summary = "Decline a workspace invitation",
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
    @PutMapping("/invite/{inviteId}/decline")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> declineWorkspaceInvite(@PathVariable UUID inviteId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        workspaceMembershipService.declineInvitationToWorkspace(inviteId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace invite declined successfully", null));
    }

    @Operation(
            description = "Cancel workspace invite endpoint",
            summary = "Cancel a workspace invitation",
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
    @PutMapping("/{workspaceId}/invite/{inviteId}/cancel")
    @PreAuthorize("@authorizationSecurityService.canInviteWorkspaceMembers(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> cancelWorkspaceInvite(@PathVariable UUID workspaceId, @PathVariable UUID inviteId) {
        workspaceMembershipService.cancelWorkspaceInvitation(inviteId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace invite cancelled successfully", null));
    }

    @Operation(
            description = "Get workspace invites endpoint",
            summary = "Get all pending invites for a workspace",
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
    @GetMapping("/{workspaceId}/invites")
    @PreAuthorize("@authorizationSecurityService.canInviteWorkspaceMembers(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<List<WorkspaceInviteDTO>>> getWorkspaceInvites(@PathVariable UUID workspaceId) {
        List<WorkspaceInviteDTO> invites = workspaceMembershipService.getWorkspacePendingInvites(workspaceId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace invites retrieved successfully", invites));
    }

    @Operation(
            description = "Get user workspace invites endpoint",
            summary = "Get all pending workspace invites for a user",
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
    public ResponseEntity<ApiSuccessResponseDTO<List<WorkspaceInviteDTO>>> getUserWorkspaceInvites(@AuthenticationPrincipal AppUserDetails appUserDetails) {
        List<WorkspaceInviteDTO> invites = workspaceMembershipService.getUserPendingWorkspaceInvites(appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "User workspace invites retrieved successfully", invites));
    }


    @Operation(
            description = "Change workspace member role endpoint",
            summary = "Change a member's role in a workspace",
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
    @PutMapping("/{workspaceId}/members/{memberId}/role")
    @PreAuthorize("@authorizationSecurityService.canChangeWorkspaceMemberRole(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> changeWorkspaceMemberRole(@PathVariable UUID workspaceId, @PathVariable UUID memberId, @RequestBody @Valid WorkspaceChangeRoleDTO workspaceChangeRoleDTO) {
        workspaceMembershipService.changeWorkspaceMemberRole(workspaceId, memberId, workspaceChangeRoleDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Member role changed successfully", null));
    }

    @Operation(
            description = "Transfer workspace ownership endpoint",
            summary = "Transfer ownership of a workspace",
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
    @PutMapping("/{workspaceId}/transfer-ownership/{newOwnerId}")
    @PreAuthorize("@authorizationSecurityService.isWorkspaceMember(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> transferWorkspaceOwnership(@PathVariable UUID workspaceId, @PathVariable UUID newOwnerId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        workspaceMembershipService.transferWorkspaceOwnership(workspaceId, newOwnerId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace ownership transferred successfully", null));
    }
}
