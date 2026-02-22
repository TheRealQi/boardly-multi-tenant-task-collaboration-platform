package com.boardly.common.dto.workspace;

import com.boardly.common.enums.WorkspaceRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkspaceDTO {
    private UUID workspaceId;
    private String title;
    private String description;
    private WorkspaceRole role;
}
