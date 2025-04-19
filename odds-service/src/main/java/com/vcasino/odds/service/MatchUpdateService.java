package com.vcasino.odds.service;

import com.vcasino.commonkafka.enums.MarketUpdateType;
import com.vcasino.commonkafka.enums.Topic;
import com.vcasino.commonkafka.event.MarketUpdateEvent;
import com.vcasino.commonkafka.event.MatchUpdateEvent;
import com.vcasino.odds.entity.market.Market;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class MatchUpdateService {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Async
    public <T extends Market> void sendMarketUpdate(Long matchId, List<T> markets, MarketUpdateType updateType) {
        if (markets.isEmpty()) return;

        List<Long> ids = markets.stream().map(Market::getId).toList();
        MarketUpdateEvent event = new MarketUpdateEvent(matchId, ids, updateType, LocalDateTime.now());
        kafkaTemplate.send(Topic.MARKET_UPDATE.getName(), event);
    }

    @Async
    public void sendMatchUpdate(Long matchId, boolean scoreUpdated, boolean matchEnded) {
        MatchUpdateEvent event = new MatchUpdateEvent(matchId, scoreUpdated, matchEnded);
        kafkaTemplate.send(Topic.MATCH_UPDATE.getName(), event);
    }
}
