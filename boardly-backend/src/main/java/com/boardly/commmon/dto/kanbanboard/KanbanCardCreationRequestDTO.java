package com.boardly.commmon.dto.kanbanboard;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class KanbanCardCreationRequestDTO {
    @NotBlank(message = "Title cannot be blank")
    private String title;

    @Positive(message = "Position must be a positive integer")
    private double position;

    @NotNull
    private UUID listId;
}
