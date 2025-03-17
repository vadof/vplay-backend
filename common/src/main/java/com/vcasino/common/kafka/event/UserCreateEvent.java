package com.vcasino.common.kafka.event;

public record UserCreateEvent(Long id, String username, String invitedBy) {
}
