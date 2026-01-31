package com.boardly.commmon.dto.board;

import com.boardly.commmon.dto.workspace.WorkspaceDTO;
import com.boardly.commmon.enums.BoardRole;
import com.boardly.commmon.enums.BoardVisibility;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class BoardDTO {
    private UUID boardId;
    private String title;
    private String description;
    private WorkspaceDTO workspace;
    private BoardVisibility boardVisibility;
    private BoardRole boardRole;
}
