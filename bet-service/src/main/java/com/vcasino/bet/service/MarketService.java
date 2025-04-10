package com.vcasino.bet.service;

import com.vcasino.bet.entity.market.Market;
import com.vcasino.bet.repository.MarketRepository;
import com.vcasino.commonkafka.event.MarketUpdateEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class MarketService {

    private final MarketRepository marketRepository;

    public void handleMarketUpdateEvent(MarketUpdateEvent event) {
        switch (event.updateType()) {
            case ODDS -> handleMarketOddsUpdate(event.marketIds());
            case CLOSE -> handleMarketClose(event.marketIds());
            case OPEN -> handleMarketOpen(event.marketIds());
            case RESULT -> handleMarketResult(event.marketIds());
        }
    }

    private void handleMarketOddsUpdate(List<Long> marketIds) {
        for (Long marketId : marketIds) {
            Optional<Market> optionalMarket = marketRepository.findById(marketId);
            if (optionalMarket.isEmpty()) {
                log.error("Market#{} not found", marketId);
            } else {
                log.info("Market#{} odds update -> {}", marketId, optionalMarket.get().getOdds());
            }
        }
    }

    private void handleMarketClose(List<Long> marketIds) {
        for (Long marketId : marketIds) {
            Optional<Market> optionalMarket = marketRepository.findById(marketId);
            if (optionalMarket.isEmpty()) {
                log.error("Market#{} not found", marketId);
            } else {
                log.info("Market#{} closed", marketId);
            }
        }
    }

    private void handleMarketOpen(List<Long> marketIds) {
        for (Long marketId : marketIds) {
            Optional<Market> optionalMarket = marketRepository.findById(marketId);
            if (optionalMarket.isEmpty()) {
                log.error("Market#{} not found", marketId);
            } else {
                log.info("Market#{} opened", marketId);
            }
        }
    }

    private void handleMarketResult(List<Long> marketIds) {
        for (Long marketId : marketIds) {
            Optional<Market> optionalMarket = marketRepository.findById(marketId);
            if (optionalMarket.isEmpty()) {
                log.error("Market#{} not found", marketId);
            } else {
                log.info("Market#{} result -> {}", marketId, optionalMarket.get().getResult());
            }
        }
    }

}
