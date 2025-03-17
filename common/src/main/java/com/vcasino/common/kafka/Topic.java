package com.vcasino.common.kafka;

public enum Topic {
    USER_CREATE("user-create"),
    CLICK_EVENTS("click-events"),
    CURRENCY_CONVERSION("currency-conversion"),
    PROCESSED_EVENTS("processed-events");

    private final String name;

    Topic(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
