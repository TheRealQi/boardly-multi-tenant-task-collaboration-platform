package com.boardly.commmon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class LoginResponseDTO {
    private String userPublicId;
    private String accessToken;
    private String refreshToken;
    private Long expiresAt;
}
