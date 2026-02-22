package com.boardly.common.dto.board;

import com.boardly.common.enums.BoardRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class BoardChangeRoleRequestDTO {
    @NotNull(message = "New role is required")
    private BoardRole role;
}
