package com.vcasino.clickerdata.listener;

import com.vcasino.clickerdata.service.CurrencyService;
import com.vcasino.common.kafka.event.ProcessedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class ProcessedEventsListener {

    private final CurrencyService currencyService;

    @RetryableTopic(backoff = @Backoff(value = 2000))
    @KafkaListener(
            groupId = "clicker-data-service-group",
            topics = "processed-events",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(ProcessedEvent processedEventMessage, Acknowledgment ack) {
        log.info("Received processed-event {}", processedEventMessage);
        currencyService.processEvent(processedEventMessage);
        ack.acknowledge();
    }

}
