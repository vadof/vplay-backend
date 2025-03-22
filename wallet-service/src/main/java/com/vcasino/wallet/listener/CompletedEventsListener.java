package com.vcasino.wallet.listener;

import com.vcasino.common.kafka.event.CompletedEvent;
import com.vcasino.wallet.service.EventFinisherService;
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
public class CompletedEventsListener {

    private final EventFinisherService eventFinisher;

    @RetryableTopic(backoff = @Backoff(value = 2000))
    @KafkaListener(
            groupId = "wallet-service-group",
            topics = "completed-events",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(CompletedEvent event, Acknowledgment ack) {
        log.info("Received completed event - {}", event);
        eventFinisher.completeEvent(event.eventId());
        ack.acknowledge();
    }
}
