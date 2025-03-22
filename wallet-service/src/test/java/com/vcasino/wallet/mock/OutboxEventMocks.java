package com.vcasino.wallet.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.wallet.entity.CurrencyConversionPayload;
import com.vcasino.wallet.entity.OutboxEvent;
import com.vcasino.wallet.entity.enums.Applicant;
import com.vcasino.wallet.entity.enums.EventStatus;
import com.vcasino.wallet.entity.enums.EventType;

import java.time.Instant;
import java.util.UUID;

public class OutboxEventMocks {
    public static OutboxEvent getCurrencyConversionOutboxEventMock(Long aggregateId, CurrencyConversionPayload payload) throws Exception {
        return OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(aggregateId)
                .type(EventType.CURRENCY_CONVERSION)
                .payload(new ObjectMapper().writeValueAsString(payload))
                .status(EventStatus.PENDING_CONFIRMATION)
                .applicant(Applicant.CLICKER)
                .createdAt(Instant.now())
                .version(0)
                .build();
    }
}
