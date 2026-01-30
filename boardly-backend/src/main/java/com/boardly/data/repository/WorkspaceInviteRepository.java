package com.boardly.data.repository;

import com.boardly.data.model.workspace.WorkspaceInvite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceInviteRepository extends JpaRepository<WorkspaceInvite, Integer> {
}
