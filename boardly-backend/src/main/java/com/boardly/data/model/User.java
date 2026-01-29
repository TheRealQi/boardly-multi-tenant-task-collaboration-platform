package com.boardly.data.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"user\"")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class User extends BaseEntity {
    @UuidGenerator
    @Setter(AccessLevel.NONE)
    @Column(unique = true, nullable = false, updatable = false)
    private UUID publicId;

    @Column(unique = true, nullable = false, updatable = true)
    private String username;

    @Column(unique = true, nullable = false, updatable = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, updatable = true)
    private String firstName;

    @Column(nullable = false, updatable = true)
    private String lastName;

    @Column
    private Instant lastLogin = null;

    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
    private java.util.List<UserDevice> userDevices;
}
