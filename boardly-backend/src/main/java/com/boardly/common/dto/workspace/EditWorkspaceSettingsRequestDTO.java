package com.boardly.common.dto.workspace;

import com.boardly.common.enums.BoardCreationSetting;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EditWorkspaceSettingsRequestDTO {
    @NotNull(message = "Private board creation setting is required")
    private BoardCreationSetting privateBoardCreationSetting;
    @NotNull(message = "Workspace board creation setting is required")
    private BoardCreationSetting workspaceBoardCreationSetting;
}
