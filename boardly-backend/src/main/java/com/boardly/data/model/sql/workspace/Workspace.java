package com.boardly.data.model.sql.workspace;


import com.boardly.data.model.sql.BaseEntity;
import com.boardly.data.model.sql.board.Board;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Workspace extends BaseEntity {
    @Column(nullable = false)
    private String title;

    @Column
    private String description = "";

    @Embedded
    private WorkspaceBoardCreationSetting boardCreationSettings = new WorkspaceBoardCreationSetting();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WorkspaceInvite> invites = new HashSet<>();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WorkspaceMember> members = new HashSet<>();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Board> boards = new HashSet<>();
}
