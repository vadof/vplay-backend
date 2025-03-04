package com.vcasino.user.config.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Topic {
    USER_CREATE("user-create"),
    CLICK_EVENTS("click-events");

    private final String name;
}
