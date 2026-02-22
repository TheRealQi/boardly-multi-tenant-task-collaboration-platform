package com.boardly.common.dto.kanbanboard;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class KanbanListUpdateRequestDTO {
    private String title;

    @Positive(message = "Position must be a positive integer")
    private Double position;
}
