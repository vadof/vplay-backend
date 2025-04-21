package com.vcasino.bet.service;

import com.vcasino.bet.dto.request.SetMarketResultRequest;
import com.vcasino.bet.entity.market.Market;
import com.vcasino.bet.entity.market.MarketResult;
import com.vcasino.bet.entity.market.winner.WinnerMatch;
import com.vcasino.bet.exception.AppException;
import com.vcasino.bet.repository.MarketRepository;
import com.vcasino.bet.service.bet.BetProcessingService;
import com.vcasino.commonkafka.enums.MarketUpdateType;
import com.vcasino.commonkafka.event.MarketUpdateEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class MarketService {

    private final MarketRepository marketRepository;
    private final RedisService redisService;
    private final BetProcessingService betProcessingService;

    public void handleMarketUpdateEvent(MarketUpdateEvent event) {
        if (event.updateType().equals(MarketUpdateType.RESULT)) {
            handleMarketResult(event);
        } else {
            handleMarketUpdate(event);
        }
    }

    public List<Long> setResultToMarkets(SetMarketResultRequest request) {
        if (request.getMarketIds().isEmpty()) {
            throw new AppException("Market ids are empty", HttpStatus.BAD_REQUEST);
        }

        List<Market> markets = marketRepository.findAllById(request.getMarketIds());

        Set<Long> foundIds = markets.stream()
                .map(Market::getId)
                .collect(Collectors.toSet());

        List<Long> missingIds = request.getMarketIds().stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        List<Market> marketsToSave = new ArrayList<>(markets.size());

        MarketResult marketResult = request.getMarketResult();
        for (Market market : markets) {
            if (market.getResult() != null) {
                log.warn("Market#{} already has result", market.getId());
            } else {
                market.setResult(marketResult);
                market.setClosed(true);
                marketsToSave.add(market);
            }
        }

        marketRepository.saveAll(marketsToSave);

        for (Market market : marketsToSave) {
            betProcessingService.processMarketResult(market.getId());
        }

        return missingIds;
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
        for (Long id : event.marketIds()) {
            betProcessingService.processMarketResult(id);
        }
    }

}
