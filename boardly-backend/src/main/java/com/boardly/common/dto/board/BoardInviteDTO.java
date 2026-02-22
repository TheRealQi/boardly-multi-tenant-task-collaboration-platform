package com.boardly.common.dto.board;

import com.boardly.common.dto.UserDTO;
import com.boardly.common.enums.InviteStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BoardInviteDTO {
    private UUID inviteId;
    private UUID boardId;
    private String boardTitle;
    private UserDTO invitedBy;
    private UserDTO invitee;
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

    public BoardInviteDTO(UUID inviteId, UserDTO invitedBy, UserDTO invitee, InviteStatus status, Instant expiresAt) {
        this.inviteId = inviteId;
        this.invitedBy = invitedBy;
        this.invitee = invitee;
        this.status = status;
        this.expiresAt = expiresAt;
    }


}
