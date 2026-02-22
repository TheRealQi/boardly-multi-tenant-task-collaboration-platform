package com.boardly.common.dto.authentication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class LoginResponseDTO {
    private UUID userId;
    private String accessToken;
    private String refreshToken;
    private Long expiresAt;
}
