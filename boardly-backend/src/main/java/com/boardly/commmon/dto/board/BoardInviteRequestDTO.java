package com.boardly.commmon.dto.board;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class BoardInviteRequestDTO {
    @NotBlank(message = "Email must not be blank")
    private String email;
}
