package com.vcasino.bet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.commonredis.enums.Channel;
import com.vcasino.commonredis.enums.NotificationType;
import com.vcasino.commonredis.event.NotificationEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class NotificationService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Async
    public void sendBetStatusUpdateNotifications(List<Long> userIds) {
        if (!userIds.isEmpty()) {
            for (Long userId : userIds) {
                NotificationEvent event = new NotificationEvent(NotificationType.BET, "Bet status updated", null, userId);
                try {
                    redisTemplate.convertAndSend(Channel.NOTIFICATIONS.getName(), objectMapper.writeValueAsString(event));
                } catch (JsonProcessingException e) {
                    log.error("Error converting object to string {}", event, e);
                }
            }
        }
    }

}
