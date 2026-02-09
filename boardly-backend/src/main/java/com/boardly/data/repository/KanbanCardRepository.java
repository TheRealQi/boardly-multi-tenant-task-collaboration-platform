package com.boardly.data.repository;

import com.boardly.data.model.nosql.KanbanCard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KanbanCardRepository extends MongoRepository<KanbanCard, UUID> {
    List<KanbanCard> findAllByBoardId(UUID boardId);
    void deleteAllByBoardId(UUID boardId);
    void deleteAllByBoardIdAndListId(UUID boardId, UUID listId);
}
