package com.boardly.data.model.sql.workspace;

import com.boardly.commmon.enums.WorkspaceRole;
import com.boardly.data.model.sql.BaseEntity;
import com.boardly.data.model.sql.authentication.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class WorkspaceMember extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_Id")
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_Id")
    private User user;

    @Enumerated(EnumType.STRING)
    private WorkspaceRole role = WorkspaceRole.MEMBER;
}
