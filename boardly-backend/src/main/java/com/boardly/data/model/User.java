package com.boardly.data.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
    private java.util.List<UserDevice> userDevices;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<WorkspaceMember> memberships = new HashSet<>();
}
