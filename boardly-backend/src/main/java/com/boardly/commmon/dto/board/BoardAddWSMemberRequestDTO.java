package com.boardly.commmon.dto.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Setter
@Getter
public class BoardAddWSMemberRequestDTO {
    @NotNull(message = "User ID must not be blank")
    private UUID userId;
}
