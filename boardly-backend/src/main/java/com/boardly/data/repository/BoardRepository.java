package com.boardly.data.repository;

import com.boardly.commmon.enums.BoardVisibility;
import com.boardly.data.model.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface BoardRepository extends JpaRepository<Board,UUID> {
    boolean existsById(UUID id);
    void deleteById(UUID id);

    Optional<Board> findById(UUID id);

    @Query("""
    SELECT b.boardVisibility
    FROM Board b
    WHERE b.Id = :boardId
""")
    Optional<BoardVisibility> findBoardVisibilityById(@Param("boardId") UUID boardId);}

