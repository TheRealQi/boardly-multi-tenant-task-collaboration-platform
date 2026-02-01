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
    private UUID workspaceId;
    private String workspaceTitle;
    private UserDTO invitee;
    private UserDTO inviter;
    private InviteStatus status;
    private Instant expiresAt;

    public WorkspaceInviteDTO(UUID inviteId, UUID workspaceId, String workspaceTitle, UserDTO inviter, InviteStatus status, Instant expiresAt) {
        this.inviteId = inviteId;
        this.workspaceId = workspaceId;
        this.workspaceTitle = workspaceTitle;
        this.inviter = inviter;
        this.status = status;
        this.expiresAt = expiresAt;
    }
}
