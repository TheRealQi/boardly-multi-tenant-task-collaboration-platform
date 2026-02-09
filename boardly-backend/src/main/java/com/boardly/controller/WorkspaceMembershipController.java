package com.boardly.controller;

import com.boardly.commmon.dto.ApiSuccessResponseDTO;
import com.boardly.commmon.dto.workspace.*;
import com.boardly.security.model.AppUserDetails;
import com.boardly.service.WorkspaceMembershipService;
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
public class WorkspaceMembershipController {
    private final WorkspaceMembershipService workspaceMembershipService;

    public WorkspaceMembershipController(WorkspaceMembershipService workspaceMembershipService) {
        this.workspaceMembershipService = workspaceMembershipService;
    }

    @GetMapping("/{workspaceId}/members") // Done
    @PreAuthorize("@authorizationSecurityService.isWorkspaceMember(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<List<WorkspaceMemberDTO>>> getWorkspaceMembers(@PathVariable UUID workspaceId) {
        List<WorkspaceMemberDTO> members = workspaceMembershipService.getWorkspaceMembers(workspaceId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace members retrieved successfully", members));
    }

    @DeleteMapping("/{workspaceId}/leave") // Done
    @PreAuthorize("@authorizationSecurityService.isWorkspaceMember(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> leaveWorkspace(@PathVariable UUID workspaceId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        workspaceMembershipService.leaveWorkspace(workspaceId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Left workspace successfully", null));
    }

    @PostMapping("/{workspaceId}/invite") // Done
    @PreAuthorize("@authorizationSecurityService.canInviteWorkspaceMembers(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> inviteMemberToWorkspace(@PathVariable UUID workspaceId, @RequestParam String email, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        workspaceMembershipService.inviteMemberToWorkspace(workspaceId, email, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Invitation sent successfully", null));
    }

    @DeleteMapping("/{workspaceId}/members/{memberId}") // Done
    @PreAuthorize("@authorizationSecurityService.canRemoveWorkspaceMembers(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> removeMemberFromWorkspace(@PathVariable UUID workspaceId, @PathVariable UUID memberId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        workspaceMembershipService.removeMemberFromWorkspace(workspaceId, memberId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Member removed successfully", null));
    }

    @PutMapping("/invite/{inviteId}/accept")
    public ResponseEntity<ApiSuccessResponseDTO<WorkspaceDTO>> acceptWorkspaceInvite( @PathVariable UUID inviteId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        WorkspaceDTO workspaceDTO = workspaceMembershipService.acceptInvitationToWorkspace(inviteId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace invite accepted successfully", workspaceDTO));
    }

    @PutMapping("/invite/{inviteId}/decline")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> declineWorkspaceInvite(@PathVariable UUID inviteId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        workspaceMembershipService.declineInvitationToWorkspace(inviteId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace invite declined successfully", null));
    }

    @PutMapping("/{workspaceId}/invite/{inviteId}/cancel")
    @PreAuthorize("@authorizationSecurityService.canInviteWorkspaceMembers(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> cancelWorkspaceInvite(@PathVariable UUID workspaceId, @PathVariable UUID inviteId) {
        workspaceMembershipService.cancelBoardInvitation(inviteId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace invite cancelled successfully", null));
    }

    @GetMapping("/{workspaceId}/invites")
    @PreAuthorize("@authorizationSecurityService.canInviteWorkspaceMembers(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<List<WorkspaceInviteDTO>>> getWorkspaceInvites(@PathVariable UUID workspaceId) {
        List<WorkspaceInviteDTO> invites = workspaceMembershipService.getWorkspacePendingInvites(workspaceId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace invites retrieved successfully", invites));
    }

    @GetMapping("/invites")
    public ResponseEntity<ApiSuccessResponseDTO<List<WorkspaceInviteDTO>>> getUserWorkspaceInvites(@AuthenticationPrincipal AppUserDetails appUserDetails) {
        List<WorkspaceInviteDTO> invites = workspaceMembershipService.getUserPendingWorkspaceInvites(appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "User workspace invites retrieved successfully", invites));
    }


    @PutMapping("/{workspaceId}/members/{memberId}/role")
    @PreAuthorize("@authorizationSecurityService.canChangeWorkspaceMemberRole(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> changeWorkspaceMemberRole(@PathVariable UUID workspaceId, @PathVariable UUID memberId, @RequestBody @Valid WorkspaceChangeRoleDTO workspaceChangeRoleDTO) {
        workspaceMembershipService.changeWorkspaceMemberRole(workspaceId, memberId, workspaceChangeRoleDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Member role changed successfully", null));
    }

    @PutMapping("/{workspaceId}/transfer-ownership/{newOwnerId}")
    @PreAuthorize("@authorizationSecurityService.isWorkspaceMember(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> transferWorkspaceOwnership(@PathVariable UUID workspaceId, @PathVariable UUID newOwnerId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        workspaceMembershipService.transferWorkspaceOwnership(workspaceId, newOwnerId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace ownership transferred successfully", null));
    }
}
