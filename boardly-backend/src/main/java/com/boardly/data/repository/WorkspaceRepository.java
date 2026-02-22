package com.boardly.data.repository;

import com.boardly.common.dto.workspace.WorkspaceDTO;
import com.boardly.data.model.sql.authentication.User;
import com.boardly.data.model.sql.workspace.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
    @Query("SELECT new com.boardly.common.dto.workspace.WorkspaceDTO(w.Id, w.title, w.description, wm.role) " +
            "FROM WorkspaceMember wm JOIN wm.workspace w " +
            "WHERE wm.user = :user")
    List<WorkspaceDTO> findAllWorkspaceDTOsByUser(@Param("user") User user);

    @Query("SELECT new com.boardly.common.dto.workspace.WorkspaceDTO(w.Id, w.title, w.description, wm.role) " +
            "FROM WorkspaceMember wm JOIN wm.workspace w " +
            "WHERE wm.workspace = :workspace AND wm.user = :user")
    Optional<WorkspaceDTO> findWorkspaceDTOByWorkspaceAndUser(@Param("workspace") Workspace workspace, @Param("user") User user);
}
