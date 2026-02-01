package com.boardly.commmon.dto.board;

import com.boardly.commmon.enums.BoardRole;
import com.boardly.commmon.enums.WorkspaceRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class BoardChangeRoleDTO {
    @NotNull(message = "New role is required")
    private BoardRole role;
}
