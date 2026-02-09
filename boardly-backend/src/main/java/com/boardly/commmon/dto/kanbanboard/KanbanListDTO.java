package com.boardly.commmon.dto.kanbanboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KanbanListDTO {
    private UUID listId;
    private String title;
    private double position;
    private List<KanbanCardDTO> cards;
}
