package com.boardly.commmon.dto.board;

import com.boardly.commmon.dto.workspace.WorkspaceDTO;
import com.boardly.commmon.enums.BoardRole;
import com.boardly.commmon.enums.BoardVisibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BoardDTO {
    private UUID boardId;
    private String title;
    private String description;
    private BoardVisibility boardVisibility;
    private BoardRole boardRole;
    private WorkspaceDTO workspace;
}
