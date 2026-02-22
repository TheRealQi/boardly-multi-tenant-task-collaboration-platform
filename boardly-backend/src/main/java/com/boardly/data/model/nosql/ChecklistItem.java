package com.boardly.data.model.nosql;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class ChecklistItem {
    private UUID id = UUID.randomUUID();
    private String text;
    private boolean isCompleted;
}
