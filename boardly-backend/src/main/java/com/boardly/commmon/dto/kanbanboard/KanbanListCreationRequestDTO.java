package com.boardly.commmon.dto.kanbanboard;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class KanbanListCreationRequestDTO {
    @NotBlank(message = "Title is required")
    private String title;

    @Positive(message = "Position must be a positive number")
    private long position;
}
