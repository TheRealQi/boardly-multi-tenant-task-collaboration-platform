package com.boardly.common.dto.kanbanboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class KanbanBoardDTO {
    private UUID boardId;
    private List<KanbanListDTO> lists;
}
