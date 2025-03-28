package com.vcasino.commonkafka.event;

public record ClickEvent(Long accountId, int amount) {}
