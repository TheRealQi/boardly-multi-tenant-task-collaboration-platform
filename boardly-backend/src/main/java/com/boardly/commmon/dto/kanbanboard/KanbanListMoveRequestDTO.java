package com.boardly.commmon.dto.kanbanboard;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class KanbanListMoveRequestDTO {
    @Positive(message = "Position must be a positive number")
    private double position;
}
