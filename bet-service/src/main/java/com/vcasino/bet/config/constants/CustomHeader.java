package com.vcasino.bet.config.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CustomHeader {
    ROLE("userRole"),
    LOGGED_IN_USER("loggedInUser");

    private final String value;
}
