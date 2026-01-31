package com.boardly.controller;

import com.boardly.commmon.dto.ApiSuccessResponseDTO;
import com.boardly.commmon.dto.workspace.*;
import com.boardly.data.model.workspace.Workspace;
import com.boardly.security.model.AppUserDetails;
import com.boardly.service.WorkspaceMembershipService;
import com.boardly.service.WorkspaceService;
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
@RequestMapping("${api.base-path}/${api.version}/workspace/{workspaceId}")
public class WorkspaceMembershipController {
    private final WorkspaceMembershipService workspaceMembershipService;

    public WorkspaceMembershipController(WorkspaceMembershipService workspaceMembershipService) {
        this.workspaceMembershipService = workspaceMembershipService;
    }

    @DeleteMapping("/leave")
    @PreAuthorize("@authorizationSecurityService.isWorkspaceMember(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> leaveWorkspace(@PathVariable UUID workspaceId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        workspaceMembershipService.leaveWorkspace(workspaceId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Left workspace successfully", null));
    }

    @PostMapping("/invite")
    @PreAuthorize("@authorizationSecurityService.canInviteWorkspaceMembers(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> inviteMemberToWorkspace(@PathVariable UUID workspaceId, @RequestParam String email, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        workspaceMembershipService.inviteMemberToWorkspace(workspaceId, email, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Invitation sent successfully", null));
    }

    @DeleteMapping("/members/{memberId}")
    @PreAuthorize("@authorizationSecurityService.canRemoveWorkspaceMembers(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> removeMemberFromWorkspace(@PathVariable UUID workspaceId, @PathVariable UUID memberId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        workspaceMembershipService.removeMemberFromWorkspace(workspaceId, memberId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Member removed successfully", null));
    }

    @PutMapping("invites/{inviteId}/accept")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> acceptWorkspaceInvite(@PathVariable UUID inviteId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        workspaceMembershipService.acceptInvitationToWorkspace(inviteId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace invite accepted successfully", null));
    }

    @PutMapping("invites/{inviteId}/decline")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> declineWorkspaceInvite(@PathVariable UUID inviteId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        workspaceMembershipService.declineInvitationToWorkspace(inviteId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace invite declined successfully", null));
    }

    @GetMapping("/invites")
    @PreAuthorize("@authorizationSecurityService.canInviteWorkspaceMembers(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<List<WorkspaceInviteDTO>>> getWorkspaceInvites(@PathVariable UUID workspaceId) {
        List<WorkspaceInviteDTO> invites = workspaceMembershipService.getWorkspacePendingInvites(workspaceId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace invites retrieved successfully", invites));
    }

    @PutMapping("/members/{memberId}/role")
    @PreAuthorize("@authorizationSecurityService.canChangeWorkspaceMemberRole(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> changeWorkspaceMemberRole(@PathVariable UUID workspaceId, @PathVariable UUID memberId, @Valid ChangeRoleDTO changeRoleDTO, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        workspaceMembershipService.changeWorkspaceMemberRole(workspaceId, memberId, changeRoleDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Member role changed successfully", null));
    }

    @PutMapping("/{workspaceId}/transfer-ownership/{newOwnerId}")
    @PreAuthorize("@authorizationSecurityService.canDeleteWorkspace(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> transferWorkspaceOwnership(@PathVariable UUID workspaceId, @PathVariable UUID newOwnerId, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        workspaceMembershipService.transferWorkspaceOwnership(workspaceId, newOwnerId, appUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace ownership transferred successfully", null));
    }
}
