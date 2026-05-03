package com.boardly.common.dto.kanbanboard;

import lombok.Getter;

@Getter
public class BoardEvent {

    public enum Type {
        LIST_CREATED, LIST_UPDATED, LIST_DELETED,
        CARD_CREATED, CARD_UPDATED, CARD_DELETED,
        COMMENT_ADDED, COMMENT_UPDATED, COMMENT_DELETED,
        CHECKLIST_ADDED, CHECKLIST_UPDATED, CHECKLIST_DELETED,
        CHECKLIST_ITEM_ADDED, CHECKLIST_ITEM_UPDATED, CHECKLIST_ITEM_DELETED
    }

    private final Type type;
    private final Object payload;

    private BoardEvent(Type type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public static BoardEvent of(Type type, Object payload) {
        return new BoardEvent(type, payload);
    }
}