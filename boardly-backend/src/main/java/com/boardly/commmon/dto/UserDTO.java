package com.boardly.commmon.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class UserDTO {
    private UUID userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePictureUrl;
}
