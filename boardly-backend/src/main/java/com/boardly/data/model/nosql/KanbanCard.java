package com.boardly.data.model.nosql;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "cards")
@CompoundIndexes({
        @CompoundIndex(def = "{'boardId': 1, 'listId': 1, 'position': 1}"),
        @CompoundIndex(def = "{'assignedMembers': 1}")
})
public class KanbanCard {
    @Id
    private UUID id;

    @Indexed
    private UUID boardId;

    @Indexed
    private UUID listId;

    private String title;
    private String description = "";

    private double position;

    private Instant startDate = null;
    private Instant dueDate = null;

    private Set<UUID> assignedMembers = new HashSet<>();

    private Set<String> labels = new HashSet<>();

    private List<Checklist> checklists = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
