package com.boardly.data.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "user_device")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDevice extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    private String deviceDetails;

    @NotNull
    private String ipAddress;

    private Instant lastLoggedInOn;

    private String refreshToken;

    private Instant refreshTokenExpiresAt;

    private Instant refreshedOn;
}
