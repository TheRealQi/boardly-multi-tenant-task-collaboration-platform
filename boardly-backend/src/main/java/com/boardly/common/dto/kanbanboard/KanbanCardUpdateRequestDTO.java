package com.boardly.common.dto.kanbanboard;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class KanbanCardUpdateRequestDTO {
    private String title;
    private String description;
    private Double position;
    private UUID listId;
    private Instant startDate;
    private Instant dueDate;
    private Set<String> labels;
    private List<UUID> assignedMembers;
}
