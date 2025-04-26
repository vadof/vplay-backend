package com.vcasino.notification.listener;

import com.vcasino.notification.service.WebSocketService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
@AllArgsConstructor
@Slf4j
public class WebSocketSubscribeListener implements ApplicationListener<SessionSubscribeEvent> {

    private final WebSocketService webSocketService;

    @Override
    public void onApplicationEvent(@NonNull SessionSubscribeEvent event) {
        this.webSocketService.subscribeToNotificationsTopic(event);
    }
}
