package com.boardly.commmon.dto.board;

import com.boardly.commmon.dto.UserDTO;
import com.boardly.commmon.enums.InviteStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BoardInviteDTO {
    private UUID inviteId;
    private UUID boardId;
    private String boardTitle;
    private UserDTO invitedBy;
    private UserDTO inviteeId;
    private InviteStatus status;
    private Instant expiresAt;

    public BoardInviteDTO(UUID inviteId, UUID boardId, String boardTitle, UserDTO invitedBy, InviteStatus status, Instant expiresAt) {
        this.inviteId = inviteId;
        this.boardId = boardId;
        this.boardTitle = boardTitle;
        this.invitedBy = invitedBy;
        this.status = status;
        this.expiresAt = expiresAt;
    }
}
