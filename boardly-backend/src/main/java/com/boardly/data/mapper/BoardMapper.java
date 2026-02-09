package com.boardly.data.mapper;

import com.boardly.commmon.dto.board.BoardCreationRequestDTO;
import com.boardly.commmon.dto.board.BoardDTO;
import com.boardly.commmon.dto.workspace.WorkspaceDTO;
import com.boardly.commmon.enums.BoardRole;
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
    Board toEntity(BoardCreationRequestDTO creationDTO);
}
