package com.boardly.common.dto.kanbanboard;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ChecklistItemDTO {
    private UUID id;
    private String text;
    private boolean completed;
}
