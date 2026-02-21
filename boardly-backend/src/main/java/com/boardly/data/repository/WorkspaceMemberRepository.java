package com.boardly.data.repository;

import com.boardly.commmon.enums.WorkspaceRole;
import com.boardly.data.model.sql.authentication.User;
import com.boardly.data.model.sql.workspace.Workspace;
import com.boardly.data.model.sql.workspace.WorkspaceMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, UUID> {

    @Query("""
        SELECT wm.role
        FROM WorkspaceMember wm
        WHERE wm.workspace.Id = :workspaceId
          AND wm.user.Id = :userId
    """)
    Optional<WorkspaceRole> findRoleByWorkspaceIdAndUserId(
            @Param("workspaceId") UUID workspaceId,
            @Param("userId") UUID userId
    );

    boolean existsByWorkspace_IdAndUser_Id(UUID workspaceId, UUID userId);
    boolean existsByWorkspaceAndUser(Workspace workspace, User user);

    Optional<WorkspaceMember> findByWorkspace_IdAndUser_Id(UUID workspaceId, UUID userId);

    @EntityGraph(attributePaths = {"user"})
    List<WorkspaceMember> findAllByWorkspace_Id(UUID workspaceId);

    boolean existsByWorkspace_IdAndUser_IdAndRole(UUID workspaceId, UUID userId, WorkspaceRole role);

    void deleteByWorkspace_IdAndUser_Id(UUID workspaceId, UUID userId);
}
