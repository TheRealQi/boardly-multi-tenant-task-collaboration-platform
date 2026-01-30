package com.boardly.commmon.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BoardCreationSetting {
    ADMINS_ONLY("admins_only"),
    ANY_MEMBER("any_member");

    BoardCreationSetting(String value) {
        this.value = value;
    }

    @JsonValue
    private final String value;
}
