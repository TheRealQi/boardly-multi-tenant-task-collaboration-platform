package com.boardly.data.repository;

import com.boardly.commmon.enums.InviteStatus;
import com.boardly.data.model.board.BoardInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BoardInviteRepository extends JpaRepository<BoardInvite, UUID> {
    boolean existsByBoard_IdAndInvitee_IdAndStatus(UUID boardId, UUID userId, InviteStatus status);
    Optional<BoardInvite> findAllByInvitee_IdAndStatus(UUID userId, InviteStatus status);
    Optional<BoardInvite> findAllByBoard_IdAndStatus(UUID boardId, InviteStatus status);
    Optional<BoardInvite> findByIdAndInvitee_Id(UUID invitationId, UUID userId);
    Optional<BoardInvite> findByIdAndBoard_IdAndInvitee_Id(UUID Id, UUID boardId, UUID userId);
}
