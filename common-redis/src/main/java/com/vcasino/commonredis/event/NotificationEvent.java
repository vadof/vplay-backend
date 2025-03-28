package com.vcasino.commonredis.event;

import com.vcasino.commonredis.enums.NotificationType;

public record NotificationEvent(NotificationType type, String message, Object data, Long userId) {
}
