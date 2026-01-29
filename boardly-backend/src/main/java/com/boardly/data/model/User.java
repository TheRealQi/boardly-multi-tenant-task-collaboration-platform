package com.boardly.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
}
