package com.boardly.data.repository;

import com.boardly.commmon.enums.InviteStatus;
import com.boardly.data.model.workspace.WorkspaceInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkspaceInviteRepository extends JpaRepository<WorkspaceInvite, Integer> {
    boolean existsByWorkspace_IdAndInvitee_IdAndStatus(UUID workspaceId, UUID inviteeId, InviteStatus status);
    Optional<WorkspaceInvite> findAllByInvitee_IdAndStatus(UUID userId, InviteStatus status);
    Optional<WorkspaceInvite> findAllByWorkspace_IdAndStatus(UUID workspaceId, InviteStatus status);
    Optional<WorkspaceInvite> findByIdAndInvitee_Id(UUID invitationId, UUID userId);
}