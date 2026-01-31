package com.boardly.data.repository;

import com.boardly.commmon.enums.BoardRole;
import com.boardly.data.model.board.BoardMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BoardMemberRepository extends JpaRepository<BoardMember, UUID> {
    @Query("""
    SELECT bm.role
    FROM BoardMember bm
    WHERE bm.board.Id = :boardId
      AND bm.user.Id = :userId
""")
    Optional<BoardRole> findRoleByBoardIdAndUserId(
            @Param("boardId") UUID boardId,
            @Param("userId") UUID userId
    );
    boolean existsByBoard_IdAndUser_Id(UUID boardId, UUID userId);
}
