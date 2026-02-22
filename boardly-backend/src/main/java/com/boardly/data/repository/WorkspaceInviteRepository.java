package com.boardly.data.repository;

import com.boardly.common.enums.InviteStatus;
import com.boardly.data.model.sql.authentication.User;
import com.boardly.data.model.sql.workspace.WorkspaceInvite;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceInviteRepository extends JpaRepository<WorkspaceInvite, UUID> {
    boolean existsByWorkspace_IdAndInvitee_IdAndStatus(UUID workspaceId, UUID inviteeId, InviteStatus status);

    @EntityGraph(attributePaths = {"workspace", "inviter"})
    List<WorkspaceInvite> findAllByInviteeAndStatus(User user, InviteStatus status);

    @EntityGraph(attributePaths = {"invitee", "inviter"})
    List<WorkspaceInvite> findAllByWorkspace_IdAndStatus(UUID workspaceId, InviteStatus status);

    Optional<WorkspaceInvite> findByIdAndInvitee_Id(UUID invitationId, UUID userId);

    Optional<WorkspaceInvite> findById(UUID invitationId);
}
