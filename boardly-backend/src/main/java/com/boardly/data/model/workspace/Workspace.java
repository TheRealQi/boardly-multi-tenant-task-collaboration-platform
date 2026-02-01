package com.boardly.data.model.workspace;


import com.boardly.data.model.BaseEntity;
import com.boardly.data.model.board.Board;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    private List<WorkspaceInvite> invites = new ArrayList<>();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WorkspaceMember> members = new HashSet<>();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> boards = new ArrayList<>();
}
