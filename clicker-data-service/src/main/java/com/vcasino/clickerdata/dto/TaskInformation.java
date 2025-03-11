package com.vcasino.clickerdata.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskInformation {
    Integer id;
    String type;
    String name;
    String link;
    Integer durationInSeconds;
    String serviceName;
    Integer rewardCoins;
    LocalDateTime validFrom;
    LocalDateTime endsIn;
    LocalDateTime createdAt;
    Integer completedTimes;
    Boolean active;
}
