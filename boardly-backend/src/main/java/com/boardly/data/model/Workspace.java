package com.boardly.data.model;


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

    @Column(nullable = true)
    private String description;

    @Embedded
    private WorkspaceSettings settings = new WorkspaceSettings();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WorkspaceMember> members = new HashSet<>();
}
