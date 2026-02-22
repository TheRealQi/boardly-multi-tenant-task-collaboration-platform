package com.boardly.common.dto.kanbanboard;

import com.boardly.data.model.nosql.Checklist;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class KanbanCardDTO {
    private UUID cardId;
    private String title;
    private UUID listId;
    private double position;
    private Instant startDate = null;
    private Instant dueDate = null;
    private List<String> labels = new ArrayList<>();
    private List<Checklist> checklists = new ArrayList<>();
    private List<UUID> assignedMembers = new ArrayList<>();
}
