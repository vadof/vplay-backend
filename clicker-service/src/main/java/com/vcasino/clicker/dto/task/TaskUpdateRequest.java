package com.vcasino.clicker.dto.task;

import com.vcasino.clicker.dto.DateRange;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskUpdateRequest {
    String name;
    Integer rewardCoins;
    DateRange dateRange;
}
