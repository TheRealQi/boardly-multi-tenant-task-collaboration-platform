package com.boardly.commmon.dto.kanbanboard;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class KanbanListUpdateRequestDTO {
    @NotBlank(message = "Title is required")
    private String title;
}
