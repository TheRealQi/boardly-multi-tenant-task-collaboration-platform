package com.boardly.commmon.dto.workspace;

import com.boardly.commmon.enums.BoardCreationSetting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EditWorkspaceRequestDTO {
    private String title;
    private String description;
    private BoardCreationSetting privateBoardCreationSetting;
    private BoardCreationSetting workspaceBoardCreationSetting;
}
