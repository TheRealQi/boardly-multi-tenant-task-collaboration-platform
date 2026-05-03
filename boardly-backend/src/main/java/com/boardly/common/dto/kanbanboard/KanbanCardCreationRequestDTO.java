package com.boardly.common.dto.kanbanboard;

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

    @NotNull
    private UUID listId;
}
