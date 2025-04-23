package com.vcasino.bet.scheduler;

import com.vcasino.bet.repository.BetRepository;
import com.vcasino.bet.service.bet.BetProcessingService;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class BetResultScheduler {

    private final BetProcessingService betProcessingService;
    private final BetRepository betRepository;
    private final RedissonClient redissonClient;

    @Scheduled(cron = "0 */15 * * * *")
    public void calculateUnsettledBets() {
        RLock lock = redissonClient.getLock("esport-scheduler-unsettled-bets");
        if (lock.tryLock()) {
            try {
                List<Long> marketIdsWithUnsettledBetsAndResolvedMarkets =
                        betRepository.findDistinctMarketIdsWithUnsettledBetsAndResolvedMarkets();

                if (!marketIdsWithUnsettledBetsAndResolvedMarkets.isEmpty()) {
                    log.info("Found {} markets with unsettled bets", marketIdsWithUnsettledBetsAndResolvedMarkets.size());
                    for (Long marketId : marketIdsWithUnsettledBetsAndResolvedMarkets) {
                        betProcessingService.processMarketResult(marketId);
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @PostConstruct
    public void init() {
        calculateUnsettledBets();
    }

}
