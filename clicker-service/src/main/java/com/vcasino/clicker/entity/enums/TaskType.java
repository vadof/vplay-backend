package com.vcasino.clicker.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.vcasino.clicker.config.IntegratedService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum TaskType {
    WATCH("Watch"),
    SUBSCRIBE("Subscribe");

    private final String value;

    TaskType(String value) {
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
    public static TaskType fromValue(String value) {
        return TaskType.valueOf(value.toUpperCase());
    }

    public boolean serviceIsSupported(IntegratedService integratedService) {
        return switch (this) {
            case WATCH -> switch (integratedService) {
                case TELEGRAM -> false;
                case YOUTUBE -> true;
            };
            case SUBSCRIBE -> true;
        };
    }
}
