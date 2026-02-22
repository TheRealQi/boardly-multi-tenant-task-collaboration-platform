package com.boardly.common.dto.workspace;

import com.boardly.common.enums.WorkspaceRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class WorkspaceChangeRoleDTO {
    @NotNull(message = "New role is required")
    private WorkspaceRole role;
}
