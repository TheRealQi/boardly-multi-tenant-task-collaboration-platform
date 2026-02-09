package com.boardly.data.model.nosql;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class ChecklistItem {
    private String Id = UUID.randomUUID().toString();
    private String text;
    private boolean isCompleted;
}
