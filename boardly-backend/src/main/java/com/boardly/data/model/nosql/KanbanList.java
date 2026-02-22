package com.boardly.data.model.nosql;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class KanbanList {
    private UUID Id;
    private String title = "";
    private double position;
}
