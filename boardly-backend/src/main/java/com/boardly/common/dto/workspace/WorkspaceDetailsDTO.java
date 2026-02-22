package com.boardly.common.dto.workspace;

import com.boardly.common.enums.BoardCreationSetting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceDetailsDTO {
    private UUID workspaceId;
    private String title;
    private String description;
    private BoardCreationSetting privateBoardCreationSetting;
    private BoardCreationSetting workspaceBoardCreationSetting;
}
