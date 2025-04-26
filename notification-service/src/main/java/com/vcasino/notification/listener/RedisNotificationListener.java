package com.vcasino.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.commonredis.event.NotificationEvent;
import com.vcasino.notification.service.WebSocketService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@AllArgsConstructor
@Slf4j
public class RedisNotificationListener implements MessageListener {

    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            NotificationEvent event = objectMapper.readValue(json, NotificationEvent.class);
            webSocketService.sendMessageToUser(event);
        } catch (Exception e) {
            log.error("Error receiving message from Redis", e);
        }
    }
}
