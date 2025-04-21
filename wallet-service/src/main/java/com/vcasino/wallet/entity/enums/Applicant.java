package com.vcasino.wallet.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Applicant {
    CLICKER("CLICKER"),
    BET("BET"),
    WALLET("WALLET");

    private final String name;

    Applicant(String name) {
        this.name = name;
    }

    @JsonCreator
    public static Applicant fromString(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Value cannot be empty");
        }

        Applicant applicant = Applicant.valueOf(value.toUpperCase());

        if (applicant == null) {
            throw new RuntimeException("Unknown applicant " + value);
        }

        return applicant;
    }

    @JsonValue
    public String getName() {
        return name;
    }
}
