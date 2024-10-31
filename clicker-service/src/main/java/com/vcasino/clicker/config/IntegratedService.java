package com.vcasino.clicker.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IntegratedService {
    YOUTUBE("YouTube"),
    TELEGRAM("Telegram");

    private final String value;

    IntegratedService(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @JsonValue
    public String toDisplayName() {
        return value;
    }

    @JsonCreator
    public static IntegratedService fromValue(String value) {
        return IntegratedService.valueOf(value.toUpperCase());
    }
}
