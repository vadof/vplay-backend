package com.vcasino.commonkafka.event;

public record UserCreateEvent(Long id, String username, String invitedBy) {
}
