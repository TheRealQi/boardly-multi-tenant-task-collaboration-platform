package com.boardly.data.repository;
import com.boardly.commmon.dto.board.BoardDTO;
import com.boardly.data.model.sql.board.Board;
import com.boardly.data.model.sql.workspace.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardRepository extends JpaRepository<Board,UUID> {
    boolean existsById(UUID id);
    void deleteById(UUID id);

    Optional<Board> findById(UUID id);

    void deleteAllByWorkspace(Workspace workspaceId);

    List<Board> findByWorkspace(Workspace workspace);

    @Query("""
    SELECT b
    FROM Board b
    LEFT JOIN BoardMember bm 
    ON bm.board.Id = b.Id AND bm.user.Id = :userId
    JOIN WorkspaceMember wm
    ON wm.workspace.Id = b.workspace.Id AND wm.user.Id = :userId
    WHERE b.workspace.Id = :workspaceId
        AND (
          wm.role IN (com.boardly.commmon.enums.WorkspaceRole.OWNER, com.boardly.commmon.enums.WorkspaceRole.ADMIN)
          OR b.boardVisibility = com.boardly.commmon.enums.BoardVisibility.WORKSPACE
          OR bm.role IS NOT NULL
        )
    """)
    List<Board> findAllViewableBoardsByUserAndWorkspace(@Param("workspaceId") UUID workspaceId, @Param("userId") UUID userId);

    @Query("""
    SELECT b
    FROM Board b
    JOIN BoardMember bm 
    ON bm.board.Id = b.Id 
    WHERE bm.user.Id = :userId
    """)
    List<Board> findAllByUser(UUID userId);

    @Query("""
        SELECT new com.boardly.commmon.dto.board.BoardDTO(b.id, b.title, b.description, b.boardVisibility, bm.role, 
            new com.boardly.commmon.dto.workspace.WorkspaceDTO(w.Id, w.title, w.description, wm.role))
        FROM Board b
        JOIN b.workspace w
        JOIN BoardMember bm ON bm.board = b
        JOIN WorkspaceMember wm ON wm.workspace = w AND wm.user.id = :userId
        WHERE bm.user.id = :userId
    """)
    List<BoardDTO> findAllBoardDTOsByUser(@Param("userId") UUID userId);

    @Query("""
        SELECT new com.boardly.commmon.dto.board.BoardDTO(b.id, b.title, b.description, b.boardVisibility, bm.role, null)
        FROM Board b
        LEFT JOIN BoardMember bm ON bm.board.id = b.id AND bm.user.id = :userId
        JOIN WorkspaceMember wm ON wm.workspace.id = b.workspace.id AND wm.user.id = :userId
        WHERE b.workspace.id = :workspaceId
        AND (
            wm.role IN (com.boardly.commmon.enums.WorkspaceRole.OWNER, com.boardly.commmon.enums.WorkspaceRole.ADMIN)
            OR b.boardVisibility = com.boardly.commmon.enums.BoardVisibility.WORKSPACE
            OR bm.role IS NOT NULL
        )
    """)
    List<BoardDTO> findAllViewableBoardDTOsByUserAndWorkspace(@Param("workspaceId") UUID workspaceId, @Param("userId") UUID userId);
}



