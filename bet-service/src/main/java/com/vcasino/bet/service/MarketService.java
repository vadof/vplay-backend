package com.vcasino.bet.service;

import com.vcasino.bet.entity.market.Market;
import com.vcasino.bet.entity.market.winner.WinnerMatch;
import com.vcasino.bet.repository.MarketRepository;
import com.vcasino.commonkafka.enums.MarketUpdateType;
import com.vcasino.commonkafka.event.MarketUpdateEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class MarketService {

    private final MarketRepository marketRepository;
    private final RedisService redisService;

    public void handleMarketUpdateEvent(MarketUpdateEvent event) {
        if (event.updateType().equals(MarketUpdateType.RESULT)) {
            handleMarketResult(event);
        } else {
            handleMarketUpdate(event);
        }
    }

    private void handleMarketUpdate(MarketUpdateEvent event) {
        List<Market> markets = marketRepository.findMarketsByIds(event.marketIds());
        if (markets.isEmpty()) {
            log.error("Markets {} not found for {} update type", event.marketIds(), event.updateType());
            return;
        }

        redisService.updateTournamentMatchMarkets(event.matchId(), markets);

        List<Market> winnerMatchMarkets = new ArrayList<>();

        for (Market market : markets) {
            if (market instanceof WinnerMatch) {
                winnerMatchMarkets.add(market);
            }
        }

        if (!winnerMatchMarkets.isEmpty()) {
            redisService.publishUpdatedMatchEvent(event.matchId(), winnerMatchMarkets, null, false);
        }

        redisService.publishUpdatedMarketsEvent(event.matchId(), markets);
    }

    private void handleMarketResult(MarketUpdateEvent event) {
        // TODO HANDLE MARKET RESULTS
    }

}
