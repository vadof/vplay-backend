package com.vcasino.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum Currency {
    VCOIN("VCoin"),
    VDOLLAR("VDollar");

    private final String name;

    Currency(String name) {
        this.name = name;
    }

    private static final Map<String, Currency> STRING_TO_ENUM = new HashMap<>();

    static {
        for (Currency currency : Currency.values()) {
            STRING_TO_ENUM.put(currency.getName().toLowerCase(), currency);
        }
    }

    @JsonCreator
    public static Currency fromString(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Value cannot be empty");
        }

        Currency currency = STRING_TO_ENUM.get(value.toLowerCase());
        if (currency == null) {
            throw new RuntimeException("Unknown currency " + value);
        }

        return currency;
    }

    @JsonValue
    public String getName() {
        return name;
    }
}
