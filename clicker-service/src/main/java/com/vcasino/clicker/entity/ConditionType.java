package com.vcasino.clicker.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConditionType {
    BY_UPGRADE("ByUpgrade");

    private final String type;

    public static ConditionType fromString(String type) {
        for (ConditionType conditionType : ConditionType.values()) {
            if (conditionType.getType().equals(type)) {
                return conditionType;
            }
        }
        throw new IllegalArgumentException("Unknown type: " + type);
    }
}
