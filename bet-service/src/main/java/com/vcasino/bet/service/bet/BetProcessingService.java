package com.vcasino.bet.service.bet;

import com.vcasino.bet.client.EventCreatedResponse;
import com.vcasino.bet.dto.request.BetRequest;
import com.vcasino.bet.dto.response.BetResponse;
import com.vcasino.bet.entity.Bet;
import com.vcasino.bet.entity.User;
import com.vcasino.bet.entity.market.Market;
import com.vcasino.bet.entity.market.MarketResult;
import com.vcasino.bet.exception.AppException;
import com.vcasino.bet.repository.BetRepository;
import com.vcasino.bet.repository.MarketRepository;
import com.vcasino.bet.service.NotificationService;
import com.vcasino.bet.service.TransactionService;
import com.vcasino.bet.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class BetProcessingService {

    private final MarketRepository marketRepository;
    private final UserService userService;
    private final BetRepository betRepository;
    private final TransactionService transactionService;
    private final NotificationService notificationService;

    @Transactional
    public BetResponse processBetPlace(BetRequest request, Long userId) {
        Market market = getMarketById(request.getMarketId());

        if (market.getClosed()) {
            return new BetResponse(null, false, "Market is closed");
        }

        BigDecimal requestOdds = request.getOdds().setScale(2, RoundingMode.DOWN);
        if (market.getOdds().compareTo(requestOdds) != 0 && !request.getAcceptAllOddsChanges()) {
            return new BetResponse(null, false, "Odds have changed");
        }

        User user = userService.getById(userId);

        BigDecimal amount = request.getAmount().setScale(2, RoundingMode.DOWN);

        EventCreatedResponse response = transactionService.createWithdrawalEvent(request, user);

        Bet bet = Bet.builder()
                .market(market)
                .user(user)
                .odds(market.getOdds())
                .amount(amount)
                .build();

        betRepository.save(bet);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                transactionService.sendCompletedEvent(response.getEventId());
            }
        });

        return new BetResponse(response.getUpdatedBalance(), true, null);
    }

    @Async
    @Transactional
    public void processMarketResult(Long marketId) {
        Optional<Market> marketOpt = marketRepository.findById(marketId);
        if (marketOpt.isEmpty()) {
            log.error("Market#{} not found. Cannot process result", marketId);
            return;
        }

        Market market = marketOpt.get();

        MarketResult result = market.getResult();
        if (result == null) {
            log.error("Market#{} result is null. Cannot process", market.getId());
            return;
        }

        List<Bet> bets = betRepository.findAllByMarketId(market.getId());
        if (bets.isEmpty()) return;

        BetResult betResult;
        if (result.equals(MarketResult.WIN) || result.equals(MarketResult.CANCELLED)) {
            betResult = processResultsWithDeposit(bets, result);
        } else {
            betResult = processResultsWithoutDeposit(bets, result);
        }

        if (!betResult.betsToSave.isEmpty()) {
            betRepository.saveAll(betResult.betsToSave);
        }

        BetResult finalBetResult = betResult;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                transactionService.sendCompletedEvents(finalBetResult.successEvents);
                notificationService.sendBetStatusUpdateNotifications(finalBetResult.userIds);
            }
        });
    }

    private BetResult processResultsWithDeposit(List<Bet> bets, MarketResult result) {
        List<Bet> betsToSave = new ArrayList<>(bets.size());
        List<UUID> successEvents = new ArrayList<>(bets.size());
        List<Long> userIds = new ArrayList<>(bets.size());

        for (Bet bet : bets) {
            BigDecimal toDeposit = result.equals(MarketResult.CANCELLED) ? bet.getAmount() :
                    bet.getAmount().multiply(bet.getOdds()).setScale(2, RoundingMode.DOWN);

            User user = bet.getUser();
            Optional<EventCreatedResponse> depositEventOpt = transactionService.createDepositEvent(toDeposit, user);
            if (depositEventOpt.isEmpty()) continue;

            bet.setResult(result);

            betsToSave.add(bet);
            successEvents.add(depositEventOpt.get().getEventId());
            userIds.add(user.getId());
        }

        return new BetResult(betsToSave, successEvents, userIds);
    }

    private BetResult processResultsWithoutDeposit(List<Bet> bets, MarketResult result) {
        List<Long> userIds = new ArrayList<>(bets.size());
        for (Bet bet : bets) {
            bet.setResult(result);
            userIds.add(bet.getUser().getId());
        }

        return new BetResult(bets, new ArrayList<>(), userIds);
    }

    private Market getMarketById(Long marketId) {
        return marketRepository.findById(marketId).orElseThrow(
                () -> new AppException("Market#" + marketId + " not found", HttpStatus.NOT_FOUND));
    }

    @AllArgsConstructor
    private static class BetResult {
        List<Bet> betsToSave;
        List<UUID> successEvents;
        List<Long> userIds;
    }

}
