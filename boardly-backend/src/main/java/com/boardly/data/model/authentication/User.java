package com.boardly.data.model.authentication;

import com.boardly.data.model.BaseEntity;
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

    @Column(updatable = true)
    private String firstName = "";

    @Column(updatable = true)
    private String lastName = "";

    @Column(nullable = false, updatable = true)
    private String profilePictureUri = "/images/profile_image_placeholder.jpg";

    @Column(nullable = false, updatable = true)
    private boolean emailVerified = false;

    @Column
    private Instant lastLogin = null;

    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<UserDevice> userDevices;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkspaceInvite> invites = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkspaceMember> memberships = new ArrayList<>();

    @OneToMany
    @JoinColumn(name = "user_id")
    private Set<SecureToken> secureTokens = new HashSet<>();
}
