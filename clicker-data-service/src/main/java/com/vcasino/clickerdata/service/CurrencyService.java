package com.vcasino.clickerdata.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.clickerdata.entity.EventStatus;
import com.vcasino.clickerdata.entity.EventType;
import com.vcasino.clickerdata.entity.OutboxEvent;
import com.vcasino.clickerdata.repository.OutboxEventRepository;
import com.vcasino.common.kafka.Topic;
import com.vcasino.common.kafka.event.CurrencyConversionEvent;
import com.vcasino.common.kafka.event.CurrencyConversionPayload;
import com.vcasino.common.kafka.event.ProcessedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CurrencyService {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void extractInProgressEventsAndSendAgain() {
        List<OutboxEvent> events = outboxEventRepository
                .findAllByEventTypeAndStatusAndCreatedAtBefore(
                        EventType.CURRENCY_CONVERSION, EventStatus.IN_PROGRESS, Instant.now().minusSeconds(60));

        log.info("Found {} IN_PROGRESS events", events.size());

        for (OutboxEvent event : events) {
            CurrencyConversionEvent message =
                    new CurrencyConversionEvent(event.getId(), event.getAggregateId(), fromString(event.getPayload()));
            kafkaTemplate.send(Topic.CURRENCY_CONVERSION.getName(), message);
        }
    }

    public void processEvent(ProcessedEvent processedEventMessage) {
        outboxEventRepository.updateStatusById(processedEventMessage.eventId(), EventStatus.COMPLETED);
    }

    private CurrencyConversionPayload fromString(String payload) {
        try {
            return objectMapper.readValue(payload, CurrencyConversionPayload.class);
        } catch (JsonProcessingException e) {
            log.error("Error converting payload {}", payload, e);
            throw new RuntimeException(e);
        }
    }
}
