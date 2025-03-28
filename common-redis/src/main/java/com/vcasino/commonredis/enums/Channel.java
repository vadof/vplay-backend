package com.vcasino.commonredis.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Channel {
    NOTIFICATIONS("notifications");

    private final String name;

    Channel(String name) {
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
    public static Channel fromValue(String value) {
        return Channel.valueOf(value.toUpperCase());
    }
}
