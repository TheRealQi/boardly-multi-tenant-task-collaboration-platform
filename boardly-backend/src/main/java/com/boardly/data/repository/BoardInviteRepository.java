package com.boardly.data.repository;

import com.boardly.commmon.enums.InviteStatus;
import com.boardly.data.model.sql.board.BoardInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardInviteRepository extends JpaRepository<BoardInvite, UUID> {
    boolean existsByBoard_IdAndInvitee_IdAndStatus(UUID boardId, UUID userId, InviteStatus status);
    List<BoardInvite> findAllByInvitee_IdAndStatus(UUID userId, InviteStatus status);
    List<BoardInvite> findAllByBoard_IdAndStatus(UUID boardId, InviteStatus status);
    Optional<BoardInvite> findByIdAndInvitee_Id(UUID invitationId, UUID userId);
    Optional<BoardInvite> findByIdAndBoard_IdAndInvitee_Id(UUID Id, UUID boardId, UUID userId);
}
