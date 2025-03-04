package com.vcasino.clicker.kafka.message;

public record ClickEvent(Long accountId, int amount) {}
