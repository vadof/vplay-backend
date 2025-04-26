package com.vcasino.notification.service;

import com.vcasino.commonredis.enums.NotificationType;
import com.vcasino.commonredis.event.NotificationEvent;
import com.vcasino.notification.dto.NotificationPayload;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
@Slf4j
public class WebSocketService {

    private final JwtService jwtService;
    private final Map<Long, Set<Session>> sessionsByUserId = new ConcurrentHashMap<>();
    private final Map<Session, Long> userIdBySession = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    public void addUser(SessionConnectedEvent event) {
        Map<String, String> nativeHeaders = extractNativeHeadersFromString(event.getMessage().getHeaders());
        String token = nativeHeaders.get("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return;
        }

        token = token.substring(7);
        try {
            Long userId = Long.valueOf(jwtService.validateTokenAndGetUserId(token));

            Session session = new Session((String) event.getMessage().getHeaders().get("simpSessionId"), nativeHeaders.get("topicId"));
            sessionsByUserId.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
            userIdBySession.put(session, userId);
        } catch (Exception ignored) {}
    }

    public void removeUser(SessionDisconnectEvent event) {
        Session session = new Session((String) event.getMessage().getHeaders().get("simpSessionId"));
        Long userId = userIdBySession.remove(session);
        if (userId != null) {
            Set<Session> userSessions = sessionsByUserId.get(userId);
            if (userSessions != null) {
                userSessions.remove(session);
                if (userSessions.isEmpty()) {
                    sessionsByUserId.remove(userId);
                }
            }
        }
    }

    public void sendMessageToUser(NotificationEvent event) {
        Set<Session> sessions = sessionsByUserId.get(event.userId());
        if (sessions != null) {
            for (Session session : sessions) {
                NotificationPayload payload = new NotificationPayload(event.message(), event.type(), event.data());
                messagingTemplate.convertAndSend("/topic/notifications/" + session.topicId, payload);
            }
        }
    }

    private Map<String, String> extractNativeHeadersFromString(MessageHeaders messageHeaders) {
        String headersString = messageHeaders.toString();

        headersString = headersString.substring(headersString.indexOf("nativeHeaders"));
        headersString = headersString.substring(headersString.indexOf("{") + 1, headersString.indexOf("]}") + 1);

        Map<String, String> nativeHeaders = new HashMap<>();

        String[] headers = headersString.split(", ");
        for (String s : headers) {
            String[] keyValue = s.split("=");
            String key = keyValue[0];
            String value = keyValue[1].substring(1, keyValue[1].length() - 1);

            nativeHeaders.put(key, value);
        }

        return nativeHeaders;
    }

    public void subscribeToNotificationsTopic(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();

        if (destination != null && destination.startsWith("/topic/notifications/")) {
            Session session = new Session(sessionId);
            if (!userIdBySession.containsKey(session)) {
                NotificationPayload payload = new NotificationPayload("Not connected", NotificationType.ERROR, null);
                messagingTemplate.convertAndSend(destination, payload);
            }
        }
    }

    @ToString
    @AllArgsConstructor
    @EqualsAndHashCode
    static class Session {
        @EqualsAndHashCode.Include
        String websocketSessionId;
        @EqualsAndHashCode.Exclude
        String topicId;

        public Session(String websocketSessionId) {
            this.websocketSessionId = websocketSessionId;
        }
    }
}
