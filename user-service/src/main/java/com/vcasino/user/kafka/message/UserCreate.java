package com.vcasino.user.kafka.message;

public record UserCreate(Long id, String username, String invitedBy) {
}
