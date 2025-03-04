package com.vcasino.clickerdata.kafka.message;

public record ClickEvent(Long accountId, int amount) {}
