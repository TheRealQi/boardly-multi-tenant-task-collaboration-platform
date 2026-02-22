package com.boardly.commmon.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum InviteStatus {
    PENDING("pending"),
    ACCEPTED("accepted"),
    DECLINED("declined"),
    CANCELLED("cancelled"),
    EXPIRED("expired");

    InviteStatus(String value) {
        this.value = value;
    }

    @JsonValue
    private final String value;
}
