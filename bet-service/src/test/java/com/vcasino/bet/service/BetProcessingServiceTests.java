package com.vcasino.bet.service;

import com.vcasino.bet.client.EventCreatedResponse;
import com.vcasino.bet.dto.request.BetRequest;
import com.vcasino.bet.dto.response.BetResponse;
import com.vcasino.bet.entity.Bet;
import com.vcasino.bet.entity.User;
import com.vcasino.bet.entity.market.Market;
import com.vcasino.bet.entity.market.MarketResult;
import com.vcasino.bet.exception.AppException;
import com.vcasino.bet.mock.MarketMocks;
import com.vcasino.bet.mock.MatchMocks;
import com.vcasino.bet.repository.BetRepository;
import com.vcasino.bet.repository.MarketRepository;
import com.vcasino.bet.service.bet.BetProcessingService;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UNIT tests for {@link BetProcessingService}
 */
@ExtendWith(MockitoExtension.class)
public class BetProcessingServiceTests {

    @Mock
    MarketRepository marketRepository;
    @Mock
    UserService userService;
    @Mock
    BetRepository betRepository;
    @Mock
    TransactionService transactionService;
    @Mock
    NotificationService notificationService;

    @InjectMocks
    BetProcessingService betProcessingService;

    @Captor
    ArgumentCaptor<Bet> betArgumentCaptor;

    @Test
    @DisplayName("Process bet place")
    void processBetPlace() {
        Market market = MarketMocks.getMarketPairMocks(MatchMocks.getMatchMock(1L), "WinnerMatch", 1).getFirst();
        when(marketRepository.findById(market.getId())).thenReturn(Optional.of(market));

        BetRequest request = new BetRequest(market.getId(), market.getOdds(), BigDecimal.ONE, false);

        User user = new User(1L, false);
        when(userService.getById(user.getId())).thenReturn(user);

        EventCreatedResponse eventCreatedResponse = new EventCreatedResponse(UUID.randomUUID(), BigDecimal.ZERO);
        when(transactionService.createWithdrawalEvent(request, user)).thenReturn(eventCreatedResponse);

        try (MockedStatic<TransactionSynchronizationManager> mockedStatic = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
            mockedStatic.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(invocation -> {
                        TransactionSynchronization synchronization = invocation.getArgument(0);
                        synchronization.afterCommit();
                        return null;
                    });

            BetResponse betResponse = betProcessingService.processBetPlace(request, user.getId());

            verify(betRepository, times(1)).save(betArgumentCaptor.capture());
            verify(transactionService, times(1)).sendCompletedEvent(eventCreatedResponse.getEventId());

            assertEquals(BigDecimal.ZERO, betResponse.getUpdatedBalance());
            assertTrue(betResponse.getBetPlaced());
            assertNull(betResponse.getReason());

            Bet bet = betArgumentCaptor.getValue();
            assertEquals(1, bet.getAmount().intValue());
            assertEquals(1.9, bet.getOdds().doubleValue());
            assertNull(bet.getResult());
        }
    }

    @Test
    @DisplayName("Process bet place market closed")
    void processBetPlaceMarketClosed() {
        Market market = MarketMocks.getMarketPairMocks(MatchMocks.getMatchMock(1L), "WinnerMatch", 1).getFirst();
        market.setClosed(true);

        when(marketRepository.findById(market.getId())).thenReturn(Optional.of(market));

        BetRequest request = new BetRequest(market.getId(), market.getOdds(), BigDecimal.ONE, true);

        User user = new User(1L, false);

        BetResponse betResponse = betProcessingService.processBetPlace(request, user.getId());

        assertNull(betResponse.getUpdatedBalance());
        assertFalse(betResponse.getBetPlaced());
        assertTrue(Strings.isNotBlank(betResponse.getReason()));
    }

    @Test
    @DisplayName("Process bet place market has result")
    void processBetPlaceMarketHasResult() {
        Market market = MarketMocks.getMarketPairMocks(MatchMocks.getMatchMock(1L), "WinnerMatch", 1).getFirst();
        market.setResult(MarketResult.WIN);

        when(marketRepository.findById(market.getId())).thenReturn(Optional.of(market));

        BetRequest request = new BetRequest(market.getId(), market.getOdds(), BigDecimal.ONE, true);

        User user = new User(1L, false);

        BetResponse betResponse = betProcessingService.processBetPlace(request, user.getId());

        assertNull(betResponse.getUpdatedBalance());
        assertFalse(betResponse.getBetPlaced());
        assertTrue(Strings.isNotBlank(betResponse.getReason()));
    }

    @Test
    @DisplayName("Process bet place odds have changed")
    void processBetPlaceOddsHaveChanged() {
        Market market = MarketMocks.getMarketPairMocks(MatchMocks.getMatchMock(1L), "WinnerMatch", 1).getFirst();

        when(marketRepository.findById(market.getId())).thenReturn(Optional.of(market));

        BetRequest request = new BetRequest(market.getId(), market.getOdds().add(BigDecimal.valueOf(0.01)), BigDecimal.ONE, false);

        User user = new User(1L, false);

        BetResponse betResponse = betProcessingService.processBetPlace(request, user.getId());

        assertNull(betResponse.getUpdatedBalance());
        assertFalse(betResponse.getBetPlaced());
        assertTrue(Strings.isNotBlank(betResponse.getReason()));
    }

    @Test
    @DisplayName("Process bet place odds have changed and accept all odds changes")
    void processBetPlaceOddsHaveChangedAndAcceptAllOddsChanges() {
        Market market = MarketMocks.getMarketPairMocks(MatchMocks.getMatchMock(1L), "WinnerMatch", 1).getFirst();
        when(marketRepository.findById(market.getId())).thenReturn(Optional.of(market));

        BetRequest request = new BetRequest(market.getId(), market.getOdds().add(BigDecimal.valueOf(0.01)), BigDecimal.ONE, true);

        User user = new User(1L, false);
        when(userService.getById(user.getId())).thenReturn(user);

        EventCreatedResponse eventCreatedResponse = new EventCreatedResponse(UUID.randomUUID(), BigDecimal.ZERO);
        when(transactionService.createWithdrawalEvent(request, user)).thenReturn(eventCreatedResponse);

        try (MockedStatic<TransactionSynchronizationManager> mockedStatic = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
            mockedStatic.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(invocation -> {
                        TransactionSynchronization synchronization = invocation.getArgument(0);
                        synchronization.afterCommit();
                        return null;
                    });

            BetResponse betResponse = betProcessingService.processBetPlace(request, user.getId());

            verify(betRepository, times(1)).save(betArgumentCaptor.capture());
            verify(transactionService, times(1)).sendCompletedEvent(eventCreatedResponse.getEventId());

            assertEquals(BigDecimal.ZERO, betResponse.getUpdatedBalance());
            assertTrue(betResponse.getBetPlaced());
            assertNull(betResponse.getReason());

            Bet bet = betArgumentCaptor.getValue();
            assertEquals(1, bet.getAmount().intValue());
            assertEquals(1.9, bet.getOdds().doubleValue());
            assertNull(bet.getResult());
        }
    }

    @Test
    @DisplayName("Process bet place error to create transaction")
    void processBetPlaceErrorToCreateTransaction() {
        Market market = MarketMocks.getMarketPairMocks(MatchMocks.getMatchMock(1L), "WinnerMatch", 1).getFirst();
        when(marketRepository.findById(market.getId())).thenReturn(Optional.of(market));

        User user = new User(1L, false);
        when(userService.getById(user.getId())).thenReturn(user);

        BetRequest request = new BetRequest(market.getId(), market.getOdds(), BigDecimal.ONE, true);
        doThrow(new AppException("Bet processing failed", HttpStatus.INTERNAL_SERVER_ERROR))
                .when(transactionService).createWithdrawalEvent(request, user);

        assertThrows(AppException.class, () -> betProcessingService.processBetPlace(request, user.getId()));
    }

    @Test
    @DisplayName("Process market WIN result")
    void processMarketWinResult() {
        User user = new User(1L, false);
        Market market = MarketMocks.getMarketPairMocks(MatchMocks.getMatchMock(1L), "WinnerMatch", 1).getFirst();
        market.setResult(MarketResult.WIN);

        when(marketRepository.findById(market.getId())).thenReturn(Optional.of(market));

        Bet bet = new Bet(1L, market, user, BigDecimal.TWO, BigDecimal.ONE, null, null, null);
        List<Bet> bets = List.of(bet);

        when(betRepository.findAllByMarketId(market.getId())).thenReturn(bets);
        when(transactionService.createDepositEvent(any(), any())).thenReturn(Optional.of(new EventCreatedResponse(UUID.randomUUID(), BigDecimal.ONE)));

        try (MockedStatic<TransactionSynchronizationManager> mockedStatic = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
            mockedStatic.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(invocation -> {
                        TransactionSynchronization synchronization = invocation.getArgument(0);
                        synchronization.afterCommit();
                        return null;
                    });

            betProcessingService.processMarketResult(market.getId());

            verify(betRepository, times(1)).saveAll(anyList());
            assertEquals(MarketResult.WIN, bet.getResult());
            verify(transactionService, times(1))
                    .createDepositEvent(bet.getOdds().multiply(bet.getAmount()).setScale(2, RoundingMode.DOWN), user);
            verify(transactionService, times(1)).sendCompletedEvents(anyList());
            verify(notificationService, times(1)).sendBetStatusUpdateNotifications(anyList());
        }

    }

    @Test
    @DisplayName("Process market LOSS result")
    void processMarketLossResult() {
        User user = new User(1L, false);
        Market market = MarketMocks.getMarketPairMocks(MatchMocks.getMatchMock(1L), "WinnerMatch", 1).getFirst();
        market.setResult(MarketResult.LOSS);

        when(marketRepository.findById(market.getId())).thenReturn(Optional.of(market));

        Bet bet = new Bet(1L, market, user, BigDecimal.TWO, BigDecimal.ONE, null, null, null);
        List<Bet> bets = List.of(bet);

        when(betRepository.findAllByMarketId(market.getId())).thenReturn(bets);

        try (MockedStatic<TransactionSynchronizationManager> mockedStatic = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
            mockedStatic.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(invocation -> {
                        TransactionSynchronization synchronization = invocation.getArgument(0);
                        synchronization.afterCommit();
                        return null;
                    });

            betProcessingService.processMarketResult(market.getId());

            verify(betRepository, times(1)).saveAll(anyList());
            assertEquals(MarketResult.LOSS, bet.getResult());
            verify(notificationService, times(1)).sendBetStatusUpdateNotifications(anyList());
        }

    }

    @Test
    @DisplayName("Process market CANCELLED result")
    void processMarketCancelledResult() {
        User user = new User(1L, false);
        Market market = MarketMocks.getMarketPairMocks(MatchMocks.getMatchMock(1L), "WinnerMatch", 1).getFirst();
        market.setResult(MarketResult.CANCELLED);

        when(marketRepository.findById(market.getId())).thenReturn(Optional.of(market));

        Bet bet = new Bet(1L, market, user, BigDecimal.TWO, BigDecimal.ONE, null, null, null);
        List<Bet> bets = List.of(bet);

        when(betRepository.findAllByMarketId(market.getId())).thenReturn(bets);
        when(transactionService.createDepositEvent(any(), any())).thenReturn(Optional.of(new EventCreatedResponse(UUID.randomUUID(), BigDecimal.ONE)));

        try (MockedStatic<TransactionSynchronizationManager> mockedStatic = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
            mockedStatic.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(invocation -> {
                        TransactionSynchronization synchronization = invocation.getArgument(0);
                        synchronization.afterCommit();
                        return null;
                    });

            betProcessingService.processMarketResult(market.getId());

            verify(betRepository, times(1)).saveAll(anyList());
            assertEquals(MarketResult.CANCELLED, bet.getResult());
            verify(transactionService, times(1)).createDepositEvent(bet.getAmount(), user);
            verify(transactionService, times(1)).sendCompletedEvents(anyList());
            verify(notificationService, times(1)).sendBetStatusUpdateNotifications(anyList());
        }

    }

    @Test
    @DisplayName("Process market WIN result, deposit error")
    void processMarketWinResultDepositError() {
        User user = new User(1L, false);
        Market market = MarketMocks.getMarketPairMocks(MatchMocks.getMatchMock(1L), "WinnerMatch", 1).getFirst();
        market.setResult(MarketResult.WIN);

        when(marketRepository.findById(market.getId())).thenReturn(Optional.of(market));

        Bet bet = new Bet(1L, market, user, BigDecimal.TWO, BigDecimal.ONE, null, null, null);
        List<Bet> bets = List.of(bet);

        when(betRepository.findAllByMarketId(market.getId())).thenReturn(bets);
        when(transactionService.createDepositEvent(any(), any())).thenReturn(Optional.empty());

        try (MockedStatic<TransactionSynchronizationManager> mockedStatic = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
            mockedStatic.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(invocation -> {
                        TransactionSynchronization synchronization = invocation.getArgument(0);
                        synchronization.afterCommit();
                        return null;
                    });

            betProcessingService.processMarketResult(market.getId());

            verify(betRepository, times(0)).saveAll(anyList());
            assertNull(bet.getResult());
        }

    }

}
