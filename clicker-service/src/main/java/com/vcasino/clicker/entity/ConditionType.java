package com.vcasino.clicker.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConditionType {
    BY_UPGRADE("ByUpgrade");

    private final String type;

}
