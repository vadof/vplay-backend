package com.vcasino.clickerdata.scheduler;

import com.vcasino.clickerdata.service.CurrencyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
@Slf4j
public class CurrencyConversionScheduler {

    private final CurrencyService currencyService;
    private final RedissonClient redissonClient;

    @Scheduled(cron = "0 * * * * *") // Runs every 1 minute
    public void extractInProgressEventsAndSendAgain() {
        RLock lock = redissonClient.getLock("currency-conversion-progress");
        try {
            if (lock.tryLock(0, 30, TimeUnit.SECONDS)) {
                try {
                    log.info("Extract and send again IN_PROGRESS currency conversion events" +
                            " that have not completed within 1 minute");
                    currencyService.extractInProgressEventsAndSendAgain();
                } finally {
                    lock.unlock();
                }
            } else {
                log.info("Another instance processing IN_PROGRESS events");
            }
        } catch (InterruptedException e) {
            log.error("Lock acquisition failed: {}", e.getMessage());
        }
    }

}
