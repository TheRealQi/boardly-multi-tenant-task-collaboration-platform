package com.boardly.commmon.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum WorkspaceRole {
    OWNER("owner"),
    ADMIN("admin"),
    MEMBER("member"),
    GUEST("guest");

    WorkspaceRole(String value) {
        this.value = value;
    }

    @JsonValue
    private final String value;
}
