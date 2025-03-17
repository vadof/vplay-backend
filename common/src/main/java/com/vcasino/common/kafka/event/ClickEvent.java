package com.vcasino.common.kafka.event;

public record ClickEvent(Long accountId, int amount) {}
