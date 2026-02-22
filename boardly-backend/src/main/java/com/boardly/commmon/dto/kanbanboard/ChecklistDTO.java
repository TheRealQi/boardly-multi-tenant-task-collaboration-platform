package com.boardly.commmon.dto.kanbanboard;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChecklistDTO {
    private String id;
    private String title;
    private List<ChecklistItemDTO> items;
}
