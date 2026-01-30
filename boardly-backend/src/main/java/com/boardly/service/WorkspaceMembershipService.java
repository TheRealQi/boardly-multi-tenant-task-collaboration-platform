package com.boardly.service;

import com.boardly.commmon.enums.WorkspaceRole;
import com.boardly.data.model.workspace.WorkspaceMember;
import com.boardly.data.repository.UserRepository;
import com.boardly.data.repository.WorkspaceInviteRepository;
import com.boardly.data.repository.WorkspaceMemberRepository;
import com.boardly.data.repository.WorkspaceRepository;
import com.boardly.exception.ForbiddenException;
import com.boardly.exception.ResourceNotFoundException;
import com.boardly.security.model.AppUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WorkspaceMembershipService {
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final WorkspaceInviteRepository workspaceInviteRepository;

    public WorkspaceMembershipService(WorkspaceRepository workspaceRepository, WorkspaceMemberRepository workspaceMemberRepository, UserRepository userRepository, WorkspaceInviteRepository workspaceInviteRepository) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.userRepository = userRepository;
        this.workspaceInviteRepository = workspaceInviteRepository;
    }

    @Transactional
    public void leaveWorkspace(UUID workspaceId, AppUserDetails appUserDetails) {
        UUID userId = appUserDetails.getUserId();
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this workspace"));

        if (member.getRole() == WorkspaceRole.OWNER) {
            throw new ForbiddenException("Workspace owners cannot leave the workspace. Please transfer ownership or delete the workspace.");
        }
        workspaceMemberRepository.delete(member);
    }

    @Transactional
    public void removeMemberFromWorkspace(UUID workspaceId, UUID memberId) {
        WorkspaceMember memberToRemove = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        if (memberToRemove.getRole() == WorkspaceRole.OWNER) {
            throw new ForbiddenException("Cannot remove the workspace owner.");
        }
        workspaceMemberRepository.delete(memberToRemove);
    }

    @Transactional
    public void inviteMemberToWorkspace(UUID workspaceId, String usernameOrEmail) {

    }

    public void acceptInvitationToWorkspace(UUID workspaceId, String invitationToken) {
    }

    public void declineInvitationToWorkspace(UUID workspaceId) {
    }

    public void changeMemberRoleInWorkspace(UUID workspaceId, UUID memberId, WorkspaceRole newRole) {

    }

}
