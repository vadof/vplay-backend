package com.vcasino.user.config.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Topic {
    USER_CREATE("user-create");

    private final String name;
}
