package com.boardly.common.dto.board;

import com.boardly.common.dto.workspace.WorkspaceDTO;
import com.boardly.common.enums.BoardRole;
import com.boardly.common.enums.BoardVisibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class BoardDTO {
    private UUID boardId;
    private String title;
    private String description;
    private BoardVisibility boardVisibility;
    private BoardRole boardRole;
    private WorkspaceDTO workspace;
}
