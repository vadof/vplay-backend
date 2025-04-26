package com.vcasino.notification.listener;

import com.vcasino.notification.service.WebSocketService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

@Component
@AllArgsConstructor
@Slf4j
public class WebSocketConnectListener implements ApplicationListener<SessionConnectedEvent> {

    private final WebSocketService webSocketService;

    @Override
    public void onApplicationEvent(@NonNull SessionConnectedEvent event) {
        this.webSocketService.addUser(event);
    }
}
