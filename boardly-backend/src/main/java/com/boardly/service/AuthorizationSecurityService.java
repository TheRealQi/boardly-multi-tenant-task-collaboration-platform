package com.boardly.service;

import com.boardly.commmon.enums.BoardCreationSetting;
import com.boardly.commmon.enums.WorkspaceRole;
import com.boardly.data.model.workspace.Workspace;
import com.boardly.data.repository.WorkspaceMemberRepository;
import com.boardly.data.repository.WorkspaceRepository;
import com.boardly.exception.ForbiddenException;
import com.boardly.exception.ResourceNotFoundException;
import com.boardly.exception.UnauthorizedException;
import com.boardly.security.model.AppUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthorizationSecurityService {
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;

    public AuthorizationSecurityService(WorkspaceMemberRepository workspaceMemberRepository, WorkspaceRepository workspaceRepository) {
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.workspaceRepository = workspaceRepository;
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUserDetails userDetails) {
            return userDetails.getUserId();
        }
        throw new UnauthorizedException("User is not authenticated!");
    }

    private WorkspaceRole getCurrentUserWorkspaceRole(UUID workspaceId) {
        return workspaceMemberRepository.findRoleByWorkspaceIdAndUserId(workspaceId, getCurrentUserId())
                .orElseThrow(() -> new ForbiddenException("User is not a member of this workspace"));
    }

    public boolean isWorkspaceMember(UUID workspaceId) {
        return workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, getCurrentUserId());
    }

    public boolean canDeleteWorkspace(UUID workspaceId) {
        return getCurrentUserWorkspaceRole(workspaceId) == WorkspaceRole.OWNER;
    }

    public boolean canInviteWorkspaceMembers(UUID workspaceId) {
        WorkspaceRole role = getCurrentUserWorkspaceRole(workspaceId);
        return role == WorkspaceRole.OWNER || role == WorkspaceRole.ADMIN;
    }

    public boolean canEditWorkspace(UUID workspaceId) {
        WorkspaceRole role = getCurrentUserWorkspaceRole(workspaceId);
        return role == WorkspaceRole.OWNER || role == WorkspaceRole.ADMIN;
    }

    public boolean canCreateBoard(UUID workspaceId, boolean isPrivate) {
        WorkspaceRole role = getCurrentUserWorkspaceRole(workspaceId);
        if (role == WorkspaceRole.OWNER || role == WorkspaceRole.ADMIN) {
            return true;
        }
        if (role == WorkspaceRole.GUEST) {
            return false;
        }
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        if (isPrivate) {
            return workspace.getSettings().getPrivateBoardCreation() == BoardCreationSetting.ANY_MEMBER;
        } else {
            return workspace.getSettings().getWorkspaceVisibleBoardCreation() == BoardCreationSetting.ANY_MEMBER;
        }
    }
}
