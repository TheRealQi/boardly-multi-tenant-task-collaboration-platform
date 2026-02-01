package com.boardly.data.model.authentication;

import com.boardly.data.model.BaseEntity;
import com.boardly.data.model.board.BoardInvite;
import com.boardly.data.model.board.BoardMember;
import com.boardly.data.model.workspace.WorkspaceInvite;
import com.boardly.data.model.workspace.WorkspaceMember;
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
    private List<WorkspaceMember> workspaceMembers = new ArrayList<>();

    @OneToMany(mappedBy = "invitee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WorkspaceInvite> workspaceInvitees = new HashSet<>();

    @OneToMany(mappedBy = "inviter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkspaceInvite> workspaceInviters = new ArrayList<>();


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardMember> boardMembers = new ArrayList<>();

    @OneToMany(mappedBy = "invitee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BoardInvite> boardInvitees = new HashSet<>();

    @OneToMany(mappedBy = "inviter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardInvite> boardInviters = new ArrayList<>();

    @OneToMany
    @JoinColumn(name = "user_id")
    private Set<SecureToken> secureTokens = new HashSet<>();
}
