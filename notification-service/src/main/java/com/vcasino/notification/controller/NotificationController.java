package com.vcasino.notification.controller;

import com.vcasino.notification.service.SseEmitterService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notifications")
@AllArgsConstructor
@Slf4j
public class NotificationController {

    private final SseEmitterService sseEmitterService;
    private final HttpServletRequest request;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getNotifications() {
        Long userId = getUserId();
        log.info("Request to connect User#{}", userId);
        return sseEmitterService.createNewEmitter(userId);
    }


    protected Long getUserId() {
        String id = request.getHeader("loggedInUser");
        if (id == null) {
            log.warn("User ID header not found");
            throw new RuntimeException("User ID header not found");
        }
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", id);
            throw new RuntimeException("Invalid user ID format");
        }
    }

}
