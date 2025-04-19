package com.vcasino.commonkafka.event;

public record MatchUpdateEvent(Long matchId, boolean scoreUpdated, boolean matchEnded) {
}
