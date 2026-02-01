package com.boardly.service;

import com.boardly.commmon.enums.BoardCreationSetting;
import com.boardly.commmon.enums.BoardRole;
import com.boardly.commmon.enums.BoardVisibility;
import com.boardly.commmon.enums.WorkspaceRole;
import com.boardly.data.model.board.Board;
import com.boardly.data.model.workspace.Workspace;
import com.boardly.data.repository.BoardMemberRepository;
import com.boardly.data.repository.BoardRepository;
import com.boardly.data.repository.WorkspaceMemberRepository;
import com.boardly.data.repository.WorkspaceRepository;
import com.boardly.exception.ForbiddenException;
import com.boardly.exception.ResourceNotFoundException;
import com.boardly.exception.UnauthorizedException;
import com.boardly.security.model.AppUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthorizationSecurityService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final BoardRepository boardRepository;

    public AuthorizationSecurityService(
            WorkspaceMemberRepository workspaceMemberRepository,
            WorkspaceRepository workspaceRepository,
            BoardMemberRepository boardMemberRepository,
            BoardRepository boardRepository
    ) {
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.workspaceRepository = workspaceRepository;
        this.boardMemberRepository = boardMemberRepository;
        this.boardRepository = boardRepository;
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUserDetails userDetails) {
            return userDetails.getUserId();
        }
        throw new UnauthorizedException("You must be authenticated to perform this action.");
    }

    private WorkspaceRole getCurrentUserWorkspaceRole(UUID workspaceId) {
        return workspaceMemberRepository
                .findRoleByWorkspaceIdAndUserId(workspaceId, getCurrentUserId())
                .orElseThrow(() ->
                        new ForbiddenException("You are not a member of this workspace"));
    }

    public boolean isWorkspaceMember(UUID workspaceId) {
        return workspaceMemberRepository
                .existsByWorkspace_IdAndUser_Id(workspaceId, getCurrentUserId());
    }

    public boolean canDeleteWorkspace(UUID workspaceId) {
        if (getCurrentUserWorkspaceRole(workspaceId) != WorkspaceRole.OWNER) {
            throw new ForbiddenException("You are not allowed to delete this workspace.");
        }
        return true;
    }

    public boolean canInviteWorkspaceMembers(UUID workspaceId) {
        WorkspaceRole role = getCurrentUserWorkspaceRole(workspaceId);
        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
            throw new ForbiddenException("You are not allowed to invite members.");
        }
        return true;
    }

    public boolean canRemoveWorkspaceMembers(UUID workspaceId) {
        WorkspaceRole role = getCurrentUserWorkspaceRole(workspaceId);
        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
            throw new ForbiddenException("You are not allowed to remove members.");
        }
        return true;
    }

    public boolean canChangeWorkspaceMemberRole(UUID workspaceId) {
        WorkspaceRole role = getCurrentUserWorkspaceRole(workspaceId);
        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
            throw new ForbiddenException("You are not allowed to change member roles.");
        }
        return true;
    }

    public boolean canEditWorkspace(UUID workspaceId) {
        WorkspaceRole role = getCurrentUserWorkspaceRole(workspaceId);
        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
            throw new ForbiddenException("You are not allowed to edit the workspace.");
        }
        return true;
    }

    public boolean canCreateBoard(UUID workspaceId, BoardVisibility visibility) {
        WorkspaceRole workspaceRole = getCurrentUserWorkspaceRole(workspaceId);

        if (workspaceRole == WorkspaceRole.OWNER || workspaceRole == WorkspaceRole.ADMIN) {
            return true;
        }

        if (workspaceRole == WorkspaceRole.GUEST) {
            throw new ForbiddenException("You do not have permission to create boards in this workspace.");
        }

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found."));

        if (visibility == BoardVisibility.PRIVATE) {
            if (workspace.getBoardCreationSettings().getPrivateBoardCreation()
                    == BoardCreationSetting.ADMINS_ONLY) {
                throw new ForbiddenException(
                        "You are not allowed to create private boards in this workspace.");
            }
        } else {
            if (workspace.getBoardCreationSettings().getWorkspaceVisibleBoardCreation()
                    == BoardCreationSetting.ADMINS_ONLY) {
                throw new ForbiddenException(
                        "You are not allowed to create workspace-visible boards in this workspace.");
            }
        }

        return true;
    }

    private BoardRole getCurrentUserBoardRole(UUID boardId) {
        return boardMemberRepository
                .findRoleByBoardIdAndUserId(boardId, getCurrentUserId())
                .orElseThrow(() ->
                        new ForbiddenException("You are not a member of this board."));
    }

    public boolean isBoardMember(UUID boardId) {
        return boardMemberRepository
                .existsByBoard_IdAndUser_Id(boardId, getCurrentUserId());
    }

    public boolean canDeleteBoard(UUID boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found."));
        UUID workspaceId = board.getWorkspace().getId();
        WorkspaceRole workspaceRole = getCurrentUserWorkspaceRole(workspaceId);
        if (workspaceRole == WorkspaceRole.OWNER || workspaceRole == WorkspaceRole.ADMIN) {
            return true;
        }

        Optional<BoardRole> boardRole =
                boardMemberRepository.findRoleByBoardIdAndUserId(boardId, getCurrentUserId());

        if (boardRole.isPresent() && boardRole.get() == BoardRole.ADMIN) {
            return true;
        }

        throw new ForbiddenException("You are not allowed to delete this board.");
    }

    public boolean canViewBoard(UUID workspaceId, UUID boardId) {
        if (isBoardMember(boardId)) {
            return true;
        }

        WorkspaceRole workspaceRole = getCurrentUserWorkspaceRole(workspaceId);
        if (workspaceRole == WorkspaceRole.OWNER || workspaceRole == WorkspaceRole.ADMIN) {
            return true;
        }

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found."));

        if (board.getBoardVisibility() == BoardVisibility.WORKSPACE
                && workspaceRole != WorkspaceRole.GUEST) {
            return true;
        }

        throw new ForbiddenException("You are not allowed to view this board.");
    }

    public boolean canEditBoardContent(UUID workspaceId, UUID boardId) {
        WorkspaceRole workspaceRole = getCurrentUserWorkspaceRole(workspaceId);
        if (workspaceRole == WorkspaceRole.OWNER || workspaceRole == WorkspaceRole.ADMIN) {
            return true;
        }
        BoardRole boardRole = getCurrentUserBoardRole(boardId);
        if (boardRole == BoardRole.OBSERVER) {
            throw new ForbiddenException("You are not allowed to edit board content.");
        }
        return true;
    }

    public boolean canEditBoard(UUID boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found."));
        UUID workspaceId = board.getWorkspace().getId();
        WorkspaceRole workspaceRole = getCurrentUserWorkspaceRole(workspaceId);
        if (workspaceRole == WorkspaceRole.OWNER || workspaceRole == WorkspaceRole.ADMIN) {
            return true;
        }

        Optional<BoardRole> boardRole =
                boardMemberRepository.findRoleByBoardIdAndUserId(boardId, getCurrentUserId());

        if (boardRole.isPresent() && boardRole.get() == BoardRole.ADMIN) {
            return true;
        }

        throw new ForbiddenException("You are not allowed to edit board.");
    }

    public boolean canChangeBoardVisibility(UUID boardId, BoardVisibility newVisibility) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found."));
        UUID workspaceId = board.getWorkspace().getId();
        WorkspaceRole workspaceRole = getCurrentUserWorkspaceRole(workspaceId);
        if (workspaceRole == WorkspaceRole.OWNER || workspaceRole == WorkspaceRole.ADMIN) {
            return true;
        }
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found."));
        if (newVisibility == BoardVisibility.PRIVATE) {
            if (workspace.getBoardCreationSettings().getPrivateBoardCreation() == BoardCreationSetting.ADMINS_ONLY) {
                throw new ForbiddenException("You are not allowed to change board visibility to private in this workspace.");
            }
        } else {
            if (workspace.getBoardCreationSettings().getWorkspaceVisibleBoardCreation() == BoardCreationSetting.ADMINS_ONLY) {
                throw new ForbiddenException("You are not allowed to change board visibility to workspace-visible in this workspace.");
            }
        }
        BoardRole boardRole = getCurrentUserBoardRole(boardId);
        if (boardRole != BoardRole.ADMIN) {
            throw new ForbiddenException("You are not allowed to change board visibility.");
        }
        return true;
    }


    public boolean canManageBoardMembers(UUID boardId) {
        return canEditBoard(boardId);
    }

    public boolean canJoinBoard(UUID workspaceId, UUID boardId) {
        if (isBoardMember(boardId)) {
            throw new ForbiddenException("You are already a member of this board.");
        }

        WorkspaceRole workspaceRole = getCurrentUserWorkspaceRole(workspaceId);
        if (workspaceRole == WorkspaceRole.OWNER || workspaceRole == WorkspaceRole.ADMIN) {
            return true;
        }

        if (workspaceRole == WorkspaceRole.GUEST) {
            throw new ForbiddenException("You are not allowed to join this board.");
        }

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found."));

        if (board.getBoardVisibility() == BoardVisibility.WORKSPACE) {
            return true;
        }

        throw new ForbiddenException("You are not allowed to join this board.");
    }

    public boolean canLeaveBoard(UUID boardId) {
        if (!isBoardMember(boardId)) {
            throw new ForbiddenException("You are not a member of this board.");
        }
        return true;
    }
}
