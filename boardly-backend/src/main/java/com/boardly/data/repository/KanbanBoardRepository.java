package com.boardly.data.repository;

import com.boardly.data.model.nosql.KanbanBoard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface KanbanBoardRepository extends MongoRepository<KanbanBoard, UUID> {
    Optional<KanbanBoard> findByBoardId(UUID boardId);
    void deleteByBoardId(UUID boardId);
}
