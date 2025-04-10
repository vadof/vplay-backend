package com.vcasino.bet.listener;

import com.vcasino.bet.service.MarketService;
import com.vcasino.commonkafka.event.MarketUpdateEvent;
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
public class MarketUpdateListener {

    private final MarketService marketService;

    @RetryableTopic(backoff = @Backoff(value = 2000))
    @KafkaListener(
            groupId = "clicker-service-group",
            topics = "market-update",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(MarketUpdateEvent event, Acknowledgment ack) {
        marketService.handleMarketUpdateEvent(event);
        ack.acknowledge();
    }
}
