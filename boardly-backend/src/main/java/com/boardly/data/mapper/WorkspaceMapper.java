package com.boardly.data.mapper;

import com.boardly.common.dto.workspace.WorkspaceCreationDTO;
import com.boardly.common.dto.workspace.WorkspaceDTO;
import com.boardly.common.dto.workspace.WorkspaceDetailsDTO;
import com.boardly.common.enums.WorkspaceRole;
import com.boardly.data.model.sql.workspace.Workspace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkspaceMapper {
    @Mapping(target = "workspaceId", source = "workspace.id")
    @Mapping(target = "role", source = "role")
    WorkspaceDTO toDto(Workspace workspace, WorkspaceRole role);

    @Mapping(target = "workspaceId", source = "id")
    @Mapping(
            target = "privateBoardCreationSetting",
            source = "boardCreationSettings.privateBoardCreation"
    )
    @Mapping(
            target = "workspaceBoardCreationSetting",
            source = "boardCreationSettings.workspaceVisibleBoardCreation"
    )
    WorkspaceDetailsDTO toDetailsDto(Workspace workspace);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "boardCreationSettings", ignore = true)
    @Mapping(target = "invites", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "boards", ignore = true)
    Workspace toEntity(WorkspaceCreationDTO creationDTO);
}
