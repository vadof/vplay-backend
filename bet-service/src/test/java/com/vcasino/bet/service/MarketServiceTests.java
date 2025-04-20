package com.vcasino.bet.service;


import com.vcasino.bet.dto.request.SetMarketResultRequest;
import com.vcasino.bet.entity.Match;
import com.vcasino.bet.entity.market.Market;
import com.vcasino.bet.entity.market.MarketResult;
import com.vcasino.bet.mock.MarketMocks;
import com.vcasino.bet.mock.MatchMocks;
import com.vcasino.bet.repository.MarketRepository;
import com.vcasino.bet.service.bet.BetProcessingService;
import com.vcasino.commonkafka.enums.MarketUpdateType;
import com.vcasino.commonkafka.event.MarketUpdateEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UNIT tests for {@link MarketService}
 */
@ExtendWith(MockitoExtension.class)
public class MarketServiceTests {

    @Mock
    MarketRepository marketRepository;
    @Mock
    RedisService redisService;
    @Mock
    BetProcessingService betProcessingService;
    @InjectMocks
    MarketService marketService;

    @Test
    @DisplayName("Handle WinnerMatch market updates")
    void handleWinnerMatchMarketUpdates() {
        Match match = MatchMocks.getMatchMock(1L);
        List<Market> markets = MarketMocks.getMarketPairMocks(match, "WinnerMatch", 1);
        List<Long> ids = markets.stream().map(Market::getId).toList();

        when(marketRepository.findMarketsByIds(ids)).thenReturn(markets);

        MarketUpdateEvent marketUpdateEvent = new MarketUpdateEvent(match.getId(), ids, MarketUpdateType.ODDS, LocalDateTime.now());
        marketService.handleMarketUpdateEvent(marketUpdateEvent);

        verify(redisService, times(1)).updateTournamentMatchMarkets(match.getId(), markets);
        verify(redisService, times(1)).publishUpdatedMatchEvent(match.getId(), markets, null, false);
        verify(redisService, times(1)).publishUpdatedMarketsEvent(match.getId(), markets);
    }

    @Test
    @DisplayName("Handle TotalMaps market update")
    void handleTotalMapsMarketUpdate() {
        Match match = MatchMocks.getMatchMock(1L);
        List<Market> markets = MarketMocks.getMarketPairMocks(match, "TotalMaps", 1);
        List<Long> ids = markets.stream().map(Market::getId).toList();

        when(marketRepository.findMarketsByIds(ids)).thenReturn(markets);

        MarketUpdateEvent marketUpdateEvent = new MarketUpdateEvent(match.getId(), ids, MarketUpdateType.ODDS, LocalDateTime.now());
        marketService.handleMarketUpdateEvent(marketUpdateEvent);

        verify(redisService, times(1)).updateTournamentMatchMarkets(match.getId(), markets);
        verify(redisService, times(0)).publishUpdatedMatchEvent(match.getId(), markets, null, false);
        verify(redisService, times(1)).publishUpdatedMarketsEvent(match.getId(), markets);
    }

    @Test
    @DisplayName("Handle market result")
    void handleMarketResult() {
        Match match = MatchMocks.getMatchMock(1L);
        List<Market> markets = MarketMocks.getMarketPairMocks(match, "WinnerMatch", 1);
        List<Long> ids = markets.stream().map(Market::getId).toList();

        MarketUpdateEvent marketUpdateEvent = new MarketUpdateEvent(match.getId(), ids, MarketUpdateType.RESULT, LocalDateTime.now());
        marketService.handleMarketUpdateEvent(marketUpdateEvent);

        for (Long id : ids) {
            verify(betProcessingService, times(1)).processMarketResult(id);
        }
    }

    @Test
    @DisplayName("Set results to markets")
    void setResultToMarkets() {
        Match match = MatchMocks.getMatchMock(1L);
        List<Market> markets = MarketMocks.getMarketPairMocks(match, "WinnerMatch", 1);
        List<Long> ids = new ArrayList<>(markets.stream().map(Market::getId).toList());
        ids.add(888L);

        when(marketRepository.findAllById(ids)).thenReturn(markets);

        SetMarketResultRequest request = new SetMarketResultRequest(ids, MarketResult.WIN);
        List<Long> missingIds = marketService.setResultToMarkets(request);
        assertEquals(1, missingIds.size());
        assertEquals(888L, missingIds.getFirst());

        verify(marketRepository, times(1)).saveAll(markets);
        for (Market market : markets) {
            assertEquals(MarketResult.WIN, market.getResult());
            verify(betProcessingService, times(1)).processMarketResult(market.getId());
        }

    }
}
