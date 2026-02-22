package com.boardly.data.mapper;

import com.boardly.common.dto.board.BoardCreationRequestDTO;
import com.boardly.common.dto.board.BoardDTO;
import com.boardly.common.dto.workspace.WorkspaceDTO;
import com.boardly.common.enums.BoardRole;
import com.boardly.data.model.sql.board.Board;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BoardMapper {

    @Mapping(target = "boardId", source = "board.id")
    @Mapping(target = "boardRole", source = "role")
    @Mapping(target = "workspace", source = "workspacedto")
    @Mapping(target = "title", source = "board.title")
    @Mapping(target = "description", source = "board.description")
    BoardDTO toDto(Board board, BoardRole role, WorkspaceDTO workspacedto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "invites", ignore = true)
    Board toEntity(BoardCreationRequestDTO creationDTO);
}
