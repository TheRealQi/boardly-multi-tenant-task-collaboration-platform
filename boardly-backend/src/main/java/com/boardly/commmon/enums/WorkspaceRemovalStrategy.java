package com.boardly.commmon.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum WorkspaceRemovalStrategy {
    CONVERT_TO_GUEST("convert_to_guest"),
    REMOVE_COMPLETELY("remove_completely");

    WorkspaceRemovalStrategy(String value) {
        this.value = value;
    }

    @JsonValue
    private final String value;
}
