package com.vcasino.notification.service;

import com.vcasino.commonredis.event.NotificationEvent;
import com.vcasino.notification.dto.NotificationPayload;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
@Slf4j
public class SseEmitterService {
    private final ConcurrentHashMap<Long, SseEmitter> userEmitters = new ConcurrentHashMap<>();

    private void addEmitter(Long userId, SseEmitter newEmitter) {
        SseEmitter oldEmitter = userEmitters.put(userId, newEmitter);
        if (oldEmitter != null) {
            oldEmitter.complete();
        }
    }

    public void removeEmitter(Long userId, SseEmitter emitter) {
        userEmitters.compute(userId, (id, current) -> {
            if (current == emitter) {
                return null;
            }
            return current;
        });
    }

    public void sendMessageToUser(NotificationEvent event) {
        SseEmitter emitter = userEmitters.get(event.userId());
        log.info("1Send event to User#{} emitter - {}", event.userId(), emitter);
        if (emitter != null) {
            try {
                log.info("2Send event to User#{}", event.userId());
                NotificationPayload payload = new NotificationPayload(event.message(), event.type(), event.data());
                emitter.send(SseEmitter.event().data(payload));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }

    public SseEmitter createNewEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(300000L); // 5 minutes

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError((e) -> removeEmitter(userId, emitter));

        addEmitter(userId, emitter);
        return emitter;
    }
}
