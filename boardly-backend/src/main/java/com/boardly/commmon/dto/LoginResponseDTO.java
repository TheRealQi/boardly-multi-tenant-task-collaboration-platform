package com.boardly.commmon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LoginResponseDTO {
    private String userPublicId;
    private String accessToken;
    private String refreshToken;
    private Long expiresAt;
}
