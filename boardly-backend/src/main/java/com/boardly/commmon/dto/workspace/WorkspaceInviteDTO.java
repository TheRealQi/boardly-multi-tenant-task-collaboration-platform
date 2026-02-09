package com.boardly.commmon.dto.workspace;

import com.boardly.commmon.dto.UserDTO;
import com.boardly.commmon.enums.InviteStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class WorkspaceInviteDTO {
    private UUID inviteId;
    private WorkspaceDTO workspace;
    private UserDTO invitee;
    private UserDTO inviter;
    private InviteStatus status;
    private Instant expiresAt;

    public WorkspaceInviteDTO(UUID inviteId, WorkspaceDTO workspace, UserDTO inviter, InviteStatus status, Instant expiresAt) {
        this.inviteId = inviteId;
        this.workspace = workspace;
        this.inviter = inviter;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public WorkspaceInviteDTO(UUID inviteId, UserDTO invitee, UserDTO inviter, InviteStatus status, Instant expiresAt) {
        this.inviteId = inviteId;
        this.invitee = invitee;
        this.inviter = inviter;
        this.status = status;
        this.expiresAt = expiresAt;
    }
}
