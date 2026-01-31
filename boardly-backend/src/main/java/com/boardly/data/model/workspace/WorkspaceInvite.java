package com.boardly.data.model.workspace;

import com.boardly.commmon.enums.InviteStatus;
import com.boardly.data.model.BaseEntity;
import com.boardly.data.model.authentication.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceInvite extends BaseEntity {
    @Column(nullable = false)
    private UUID workspaceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status = InviteStatus.PENDING;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private UUID invitedBy;

}
