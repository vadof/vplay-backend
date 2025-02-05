package com.vcasino.user.kafka.message;

public record UserCreate(String username, String invitedBy) {
}
