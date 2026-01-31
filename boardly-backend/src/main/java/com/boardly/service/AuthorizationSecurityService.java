package com.boardly.service;

import com.boardly.commmon.enums.BoardCreationSetting;
import com.boardly.commmon.enums.BoardVisibility;
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
        if (getCurrentUserWorkspaceRole(workspaceId) != WorkspaceRole.OWNER) {
            throw new ForbiddenException("Only workspace owners can delete the workspace");
        }
        return true;
    }

    public boolean canInviteWorkspaceMembers(UUID workspaceId) {
        WorkspaceRole role = getCurrentUserWorkspaceRole(workspaceId);
        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
            throw new ForbiddenException("Only workspace owners and admins can invite members");
        }
        return true;
    }

    public boolean canRemoveWorkspaceMembers(UUID workspaceId) {
        WorkspaceRole role = getCurrentUserWorkspaceRole(workspaceId);
        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
            throw new ForbiddenException("Only workspace owners and admins can remove members");
        }
        return true;
    }

    public boolean canChangeWorkspaceMemberRole(UUID workspaceId) {
        WorkspaceRole role = getCurrentUserWorkspaceRole(workspaceId);
        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
            throw new ForbiddenException("Only workspace owners and admins can change member roles");
        }
        return true;
    }


    public boolean canEditWorkspace(UUID workspaceId) {
        WorkspaceRole role = getCurrentUserWorkspaceRole(workspaceId);
        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
            throw new ForbiddenException("Only workspace owners and admins can edit the workspace");
        }
        return true;
    }

    public boolean canCreateBoard(UUID workspaceId, BoardVisibility visibility) {
        WorkspaceRole role = getCurrentUserWorkspaceRole(workspaceId);
        if (role == WorkspaceRole.OWNER || role == WorkspaceRole.ADMIN) {
            return true;
        }
        if (role == WorkspaceRole.GUEST) {
            throw new ForbiddenException("Workspace guests are not allowed to create boards");
        }
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        if (visibility == BoardVisibility.PRIVATE) {
            if (workspace.getBoardCreationSettings().getPrivateBoardCreation() == BoardCreationSetting.ADMINS_ONLY) {
                throw new ForbiddenException("Only workspace owners and admins can create private boards");
            }
        } else {
            if (workspace.getBoardCreationSettings().getWorkspaceVisibleBoardCreation() == BoardCreationSetting.ADMINS_ONLY) {
                throw new ForbiddenException("Only workspace owners and admins can create public boards");
            }
        }
        return true;
    }
}
