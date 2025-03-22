package com.vcasino.wallet.client;

import com.vcasino.wallet.entity.enums.EventStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventStatusResponse {
    Map<UUID, EventStatus> eventStatuses;
}
