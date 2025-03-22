package com.vcasino.clickerdata.scheduler;

import com.vcasino.clickerdata.service.ClickService;
import com.vcasino.clickerdata.utils.SchedulerExecutionDate;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class ClickScheduler {

    private final ClickService clickService;
    private final RedissonClient redissonClient;
    public static final Integer EXECUTION_INTERVAL_MINUTES = 15;

    @Scheduled(cron = "0 */15 * * * *")
    public void syncClicksWithDatabase() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime lastExecution = now.minusMinutes(EXECUTION_INTERVAL_MINUTES);
        syncClicksWithDatabase(new SchedulerExecutionDate(lastExecution, now));
    }

    @PostConstruct
    public void runFailedClickSyncs() {
        List<SchedulerExecutionDate> failedDates = clickService.getFailedExecutionDates();

        if (failedDates.isEmpty()) {
            log.info("No failed executions found");
        } else {
            log.info("Found {} failed executions", failedDates.size());
            for (SchedulerExecutionDate failedDate : failedDates) {
                syncClicksWithDatabase(failedDate);
            }
        }
    }

    private void syncClicksWithDatabase(SchedulerExecutionDate executionDate) {
        RLock lock = redissonClient.getLock(executionDate.getNow().toString());
        if (lock.tryLock()) {
            try {
                log.info("Synchronize clicks with database for {} - {}",
                        executionDate.getLastExecution(), executionDate.getNow());
                clickService.syncClicksWithDatabase(executionDate);
            } finally {
                lock.unlock();
            }
        } else {
            log.info("Another instance is executing clicks syncing for {} - {}",
                    executionDate.getLastExecution(), executionDate.getNow());
        }
    }
}
