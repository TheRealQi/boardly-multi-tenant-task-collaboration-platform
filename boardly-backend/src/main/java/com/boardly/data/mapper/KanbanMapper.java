package com.boardly.data.mapper;

import com.boardly.common.dto.kanbanboard.*;
import com.boardly.data.model.nosql.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface KanbanMapper {

    @Mapping(target = "cardId", source = "id")
    KanbanCardDTO toDTO(KanbanCard kanbanCard);

    @Mapping(target = "cardId", source = "id")
    KanbanCardDetailsDTO toDetailsDTO(KanbanCard kanbanCard);

    @Mapping(target = "listId", source = "id")
    @Mapping(target = "cards", ignore = true)
    KanbanListDTO toDTO(KanbanList kanbanList);

    KanbanBoardDTO toDTO(KanbanBoard kanbanBoard);

    @Mapping(target = "comment", source = "content")
    @Mapping(target = "date", source = "createdAt")
    @Mapping(target = "author", ignore = true)
    CardCommentDTO toDTO(Comment comment);

    ChecklistDTO toDTO(Checklist checklist);

    @Mapping(target = "completed", source = "completed")
    ChecklistItemDTO toDTO(ChecklistItem checklistItem);
}
