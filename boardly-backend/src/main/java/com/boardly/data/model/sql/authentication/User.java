package com.boardly.data.model.sql.authentication;

import com.boardly.data.model.sql.BaseEntity;
import com.boardly.data.model.sql.board.BoardInvite;
import com.boardly.data.model.sql.board.BoardMember;
import com.boardly.data.model.sql.workspace.WorkspaceInvite;
import com.boardly.data.model.sql.workspace.WorkspaceMember;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "\"user\"")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class User extends BaseEntity {
    @Column(unique = true, nullable = false, updatable = true)
    private String username;

    @Column(unique = true, nullable = false, updatable = true)
    private String email;

    @Column(nullable = false, updatable = true)
    private String passwordHash;

    @Column
    private String firstName = "";

    @Column
    private String lastName = "";

    @Column(nullable = false)
    private String profilePictureUri = "/images/profile_image_placeholder.jpg";

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column
    private Instant lastLogin = null;

    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<UserDevice> userDevices;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WorkspaceMember> workspaceMembers = new HashSet<>();

    @OneToMany(mappedBy = "invitee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WorkspaceInvite> workspaceInvitees = new HashSet<>();

    @OneToMany(mappedBy = "inviter", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WorkspaceInvite> workspaceInviters = new HashSet<>();


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BoardMember> boardMembers = new HashSet<>();

    @OneToMany(mappedBy = "invitee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BoardInvite> boardInvitees = new HashSet<>();

    @OneToMany(mappedBy = "inviter", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BoardInvite> boardInviters = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SecureToken> secureTokens = new HashSet<>();
}
