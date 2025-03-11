package com.vcasino.clickerdata.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ChartOption {
    TODAY("Today"),
    LAST_WEEK("Last Week"),
    LAST_MONTH("Last Month"),
    LAST_3_MONTHS("Last 3 Months"),
    LAST_YEAR("Last Year");

    private final String displayName;

    ChartOption(String displayName) {
        this.displayName = displayName;
    }

    @JsonCreator
    public static ChartOption fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        return ChartOption.valueOf(value.toUpperCase().replace(" ", "_"));
    }

    @JsonValue
    public String toDisplayName() {
        return displayName;
    }
}
