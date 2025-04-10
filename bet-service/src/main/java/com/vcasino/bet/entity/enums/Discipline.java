package com.vcasino.bet.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum Discipline {
    COUNTER_STRIKE("Counter-Strike");

    private final String name;
    private static final Map<String, Discipline> STRING_TO_ENUM = new HashMap<>();

    Discipline(String name) {
        this.name = name;
    }

    static {
        for (Discipline game : Discipline.values()) {
            STRING_TO_ENUM.put(game.getName(), game);
        }
    }

    @JsonValue
    public String getName() {
        return name;
    }

    @JsonCreator
    public static Discipline fromValue(String name) {
        return STRING_TO_ENUM.get(name);
    }
}
