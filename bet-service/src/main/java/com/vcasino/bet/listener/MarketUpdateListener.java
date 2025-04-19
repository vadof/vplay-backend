package com.vcasino.bet.listener;

import com.vcasino.bet.service.MarketService;
import com.vcasino.bet.service.MatchService;
import com.vcasino.commonkafka.event.MarketUpdateEvent;
import com.vcasino.commonkafka.event.MatchUpdateEvent;
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
    private final MatchService matchService;

    @RetryableTopic(backoff = @Backoff(value = 2000))
    @KafkaListener(
            groupId = "bet-service-group",
            topics = "market-update",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleMarketUpdate(MarketUpdateEvent event, Acknowledgment ack) {
        marketService.handleMarketUpdateEvent(event);
        ack.acknowledge();
    }

    @RetryableTopic(backoff = @Backoff(value = 2000))
    @KafkaListener(
            groupId = "bet-service-group",
            topics = "match-update",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void MatchUpdateEvent(MatchUpdateEvent event, Acknowledgment ack) {
        matchService.handleMatchUpdateEvent(event);
        ack.acknowledge();
    }
}
