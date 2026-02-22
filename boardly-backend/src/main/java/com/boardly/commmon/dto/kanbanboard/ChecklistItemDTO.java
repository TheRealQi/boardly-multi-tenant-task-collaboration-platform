package com.boardly.commmon.dto.kanbanboard;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChecklistItemDTO {
    private String id;
    private String text;
    private boolean completed;
}
