package com.vcasino.common.kafka.event;

import java.util.UUID;

public record CurrencyConversionEvent(UUID eventId, Long aggregateId, CurrencyConversionPayload payload) {
}
