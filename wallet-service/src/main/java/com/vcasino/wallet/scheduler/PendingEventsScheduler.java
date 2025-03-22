package com.vcasino.wallet.scheduler;

import com.vcasino.wallet.service.EventService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class PendingEventsScheduler {

    private final RedissonClient redissonClient;
    private final EventService eventService;

    @Scheduled(cron = "0 */5 * * * *") // Runs every 5 minutes
    public void extractInProgressEventsAndSendAgain() {
        RLock lock = redissonClient.getLock("pending-confirmation-events");
        if (lock.tryLock()) {
            try {
                log.info("Handle PENDING_CONFIRMATION events that have not completed within 5 minutes");
                eventService.handlePendingConfirmationEvents();
            } finally {
                lock.unlock();
            }
        } else {
            log.info("Another instance processing IN_PROGRESS events");
        }
    }

}
