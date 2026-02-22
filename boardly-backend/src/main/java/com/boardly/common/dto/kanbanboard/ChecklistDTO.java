package com.boardly.common.dto.kanbanboard;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ChecklistDTO {
    private UUID id;
    private String title;
    private List<ChecklistItemDTO> items;
}
