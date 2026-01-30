package com.boardly.commmon.dto.workspace;

import com.boardly.commmon.enums.BoardCreationSetting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceSettingsDTO {
    private UUID workspaceId;
    private String title;
    private String description;
    private BoardCreationSetting privateBoardCreationSetting;
    private BoardCreationSetting workspaceBoardCreationSetting;
}
