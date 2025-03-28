package com.vcasino.commonredis.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationType {
    BALANCE("BALANCE"),
    BET("BET");

    private final String name;

    NotificationType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @JsonValue
    public String toDisplayName() {
        return name;
    }

    @JsonCreator
    public static NotificationType fromValue(String value) {
        return NotificationType.valueOf(value.toUpperCase());
    }
}
