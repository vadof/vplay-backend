package com.vcasino.commonkafka.event;

import com.vcasino.commonkafka.enums.MarketUpdateType;

import java.time.LocalDateTime;
import java.util.List;

public record MarketUpdateEvent(List<Long> marketIds, MarketUpdateType updateType, LocalDateTime updatedAt) {
}
