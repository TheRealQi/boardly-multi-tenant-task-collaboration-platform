package com.boardly.data.repository;

import com.boardly.commmon.enums.InviteStatus;
import com.boardly.data.model.workspace.WorkspaceInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkspaceInviteRepository extends JpaRepository<WorkspaceInvite, Integer> {
    boolean existsByWorkspaceIdAndUserIdAndStatus(UUID workspaceId, UUID userId, InviteStatus status);
    Optional<WorkspaceInvite> findById(UUID invitationId);
    Optional<WorkspaceInvite> findAllByUserIdAndStatus(UUID userId, InviteStatus status);
    Optional<WorkspaceInvite> findAllByWorkspaceIdAndStatus(UUID workspaceId, InviteStatus status);
    Optional<WorkspaceInvite> findByIdAndUserId(UUID invitationId, UUID userId);

}
