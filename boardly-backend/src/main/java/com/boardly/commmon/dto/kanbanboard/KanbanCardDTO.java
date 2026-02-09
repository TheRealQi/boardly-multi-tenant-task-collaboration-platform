package com.boardly.commmon.dto.kanbanboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class KanbanCardDTO {
    private UUID cardId;
    private String title;
    private UUID listId;
    private long position;
}
