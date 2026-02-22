package com.boardly.common.dto.kanbanboard;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChecklistItemUpdateRequestDTO {
    private String text;
    private Boolean completed;
}
