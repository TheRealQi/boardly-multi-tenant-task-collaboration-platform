package com.boardly.data.repository;

import com.boardly.data.model.Workspace;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WorkspaceRepository extends CrudRepository<Workspace, UUID> {
}
