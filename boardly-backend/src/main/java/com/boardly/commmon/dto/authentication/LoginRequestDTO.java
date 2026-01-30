package com.boardly.commmon.dto.authentication;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class LoginRequestDTO {
    @NotBlank
    private String usernameOrEmail;

    @NotBlank
    private String password;
}
