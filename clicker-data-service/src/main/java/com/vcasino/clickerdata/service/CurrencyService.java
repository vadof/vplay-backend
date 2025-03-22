package com.vcasino.clickerdata.service;

import com.vcasino.clickerdata.client.EventStatusRequest;
import com.vcasino.clickerdata.client.EventStatus;
import com.vcasino.clickerdata.client.EventStatusResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class CurrencyService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public EventStatusResponse getEventStatuses(EventStatusRequest request) {
        List<UUID> eventIds = request.getEventIds();
        if (eventIds.isEmpty()) {
            return new EventStatusResponse(Collections.emptyMap());
        }

        String inSql = String.join(",", Collections.nCopies(eventIds.size(), "?"));
        String sql = "SELECT event_id FROM transaction WHERE event_id IN (" + inSql + ")";

        Set<UUID> existingEventIds = new HashSet<>(jdbcTemplate.queryForList(sql, UUID.class, eventIds.toArray()));

        Map<UUID, EventStatus> eventStatuses = eventIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> existingEventIds.contains(id) ? EventStatus.COMPLETED : EventStatus.CANCELLED
                ));

        return new EventStatusResponse(eventStatuses);
    }

}
