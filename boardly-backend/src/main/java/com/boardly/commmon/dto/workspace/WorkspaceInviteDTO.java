package com.boardly.commmon.dto.workspace;

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
public class WorkspaceInviteDTO {
    private UUID inviteId;
    private UUID workspaceId;
    private String workspaceName;
    private UUID invitedBy;
    private InviteStatus status;
    private Instant expiresAt;
}
