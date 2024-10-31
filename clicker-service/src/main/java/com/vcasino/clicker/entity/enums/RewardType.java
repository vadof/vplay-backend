package com.vcasino.clicker.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.vcasino.clicker.config.IntegratedService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum RewardType {
    WATCH("Watch"),
    SUBSCRIBE("Subscribe");

    private final String value;

    RewardType(String value) {
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
    public static RewardType fromValue(String value) {
        return RewardType.valueOf(value.toUpperCase());
    }

    public boolean serviceIsSupported(IntegratedService integratedService) {
        return switch (this) {
            case WATCH -> switch (integratedService) {
                case TELEGRAM -> true;
                case YOUTUBE -> false;
            };
            case SUBSCRIBE -> true;
        };
    }
}
