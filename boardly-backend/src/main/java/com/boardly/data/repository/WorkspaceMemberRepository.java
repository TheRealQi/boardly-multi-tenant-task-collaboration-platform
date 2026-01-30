package com.boardly.data.repository;

import com.boardly.commmon.enums.WorkspaceRole;
import com.boardly.data.model.workspace.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, UUID> {
    @Query("SELECT wm.role FROM WorkspaceMember wm WHERE wm.workspace.Id = :workspaceId AND wm.user.Id = :userId")
    Optional<WorkspaceRole> findRoleByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);

    boolean existsByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);

    List<WorkspaceMember> findAllByUserId(UUID userId);

    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);
}
