package com.boardly.commmon.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class RegisterRequestDTO {
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @NotBlank
    private String username;

    @Email(message = "Email should be valid")
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 2, max = 50, message = "Are you sure about your first name?")
    private String firstName;

    private String lastName;

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 characters, include 1 uppercase letter, 1 number, and 1 special character"
    )
    private String password;
}
