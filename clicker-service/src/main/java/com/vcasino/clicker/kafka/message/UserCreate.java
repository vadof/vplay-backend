package com.vcasino.clicker.kafka.message;

public record UserCreate(Long id, String username, String invitedBy) {
}
