package com.vcasino.clickerdata.service;

import com.vcasino.clickerdata.repository.TotalClicksRepository;
import com.vcasino.clickerdata.scheduler.ClickScheduler;
import com.vcasino.clickerdata.utils.SchedulerExecutionDate;
import com.vcasino.common.kafka.event.ClickEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class ClickService {

    private final TotalClicksRepository totalClicksRepository;
    private final StringRedisTemplate redisTemplate;
    private final JdbcTemplate jdbcTemplate;

    private static final String REDIS_KEY = "clicks";

    private static final String ACCOUNT_CLICKS_UPDATE_SQL = """
            INSERT INTO account_clicks (account_id, date, amount)
            VALUES (?, ?, ?)
            ON CONFLICT (account_id, date)
            DO UPDATE SET amount = account_clicks.amount + EXCLUDED.amount
            """;

    public void handleClicks(List<ClickEvent> clicks) {
        HashOperations<String, String, Integer> hashOps = redisTemplate.opsForHash();


        for (ClickEvent click : clicks) {
            hashOps.increment(REDIS_KEY, String.valueOf(click.accountId()), click.amount());
        }
    }

    @Transactional
    public void syncClicksWithDatabase(SchedulerExecutionDate executionDate) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(REDIS_KEY))) {
            log.info("No clicks found, skipping sync");
            return;
        }

        String tempKey = REDIS_KEY + "_temp_" + executionDate.getNow().toString();
        redisTemplate.rename(REDIS_KEY, tempKey);

        moveRedisRecordsToDatabase(tempKey, executionDate);
    }

    private void moveRedisRecordsToDatabase(String tempKey, SchedulerExecutionDate executionTime) {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        Map<String, String> clicksMap = hashOps.entries(tempKey);

        log.info("Fetched {} records from Redis for {} - {}",
                clicksMap.size(), executionTime.getLastExecution(), executionTime.getNow());

        if (clicksMap.isEmpty()) {
            redisTemplate.delete(tempKey);
            return;
        }

        long totalClicks = 0;
        LocalDate date = executionTime.getLastExecution().toLocalDate();
        List<Object[]> batchArgs = new ArrayList<>(clicksMap.size());

        for (Map.Entry<String, String> record : clicksMap.entrySet()) {
            int amount = Integer.parseInt(record.getValue());
            totalClicks += amount;
            batchArgs.add(new Object[]{Long.parseLong(record.getKey()), date, amount});
        }

        jdbcTemplate.batchUpdate(ACCOUNT_CLICKS_UPDATE_SQL, batchArgs);
        totalClicksRepository.insertTotalClicks(executionTime.getLastExecution(), totalClicks);

        log.info("Click synchronization completed for {} - {} -> Accounts updated: {}, Total clicks: {}",
                executionTime.getLastExecution(), executionTime.getNow(), batchArgs.size(), totalClicks);

        redisTemplate.delete(tempKey);
    }

    public List<SchedulerExecutionDate> getFailedExecutionDates() {
        Set<String> failedSyncKeys = redisTemplate.keys(REDIS_KEY + "_temp_*");

        List<SchedulerExecutionDate> failedDates = new ArrayList<>();
        if (failedSyncKeys == null || failedSyncKeys.isEmpty()) {
            return failedDates;
        }

        for (String key : failedSyncKeys) {
            String dateStr = key.substring(key.lastIndexOf("_") + 1);
            LocalDateTime executionDate = LocalDateTime.parse(dateStr);

            failedDates.add(new SchedulerExecutionDate(executionDate.minusMinutes(ClickScheduler.EXECUTION_INTERVAL_MINUTES), executionDate));
        }

        return failedDates;
    }

}
