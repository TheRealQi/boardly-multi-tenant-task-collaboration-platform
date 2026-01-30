package com.boardly.commmon.dto;

import com.boardly.commmon.enums.InviteStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class InviteResponseDTO {
    private String email;
    private InviteStatus status;
    private Instant invitedAt;
    private Instant expiresAt;
}
