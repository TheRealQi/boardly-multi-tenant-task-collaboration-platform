package com.boardly.data.model.sql.board;

import com.boardly.common.enums.BoardVisibility;
import com.boardly.data.model.sql.BaseEntity;
import com.boardly.data.model.sql.workspace.Workspace;
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
public class Board extends BaseEntity {
    @Column(nullable = false)
    private String title;

    @Column
    private String description = "";

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BoardVisibility boardVisibility = BoardVisibility.WORKSPACE;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BoardMember> members = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BoardInvite> invites = new HashSet<>();
}
