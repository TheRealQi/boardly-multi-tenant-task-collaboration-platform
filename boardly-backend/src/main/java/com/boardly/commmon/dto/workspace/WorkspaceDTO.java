package com.boardly.commmon.dto.workspace;

import com.boardly.commmon.enums.WorkspaceRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceDTO {
    private UUID workspaceId;
    private String title;
    private String description;
    private WorkspaceRole role;
}
