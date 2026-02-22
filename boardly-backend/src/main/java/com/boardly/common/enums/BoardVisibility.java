package com.boardly.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BoardVisibility {
    PRIVATE("private"),
    WORKSPACE("workspace");

    BoardVisibility(String value) {
        this.value = value;
    }

    @JsonValue
    private final String value;
}
