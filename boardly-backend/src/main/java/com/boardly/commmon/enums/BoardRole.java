package com.boardly.commmon.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BoardRole {
    ADMIN("admin"),
    MEMBER("member"),
    OBSERVER("observer");

    BoardRole(String value) {
        this.value = value;
    }

    @JsonValue
    private final String value;
}
