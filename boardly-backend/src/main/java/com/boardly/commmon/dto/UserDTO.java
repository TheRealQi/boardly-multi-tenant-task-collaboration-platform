package com.boardly.commmon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private UUID userId;
    private String username;
    private String firstName;
    private String lastName;
    private String profilePictureUrl;
}
