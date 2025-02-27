package com.vcasino.clicker.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpgradeDto {
    String name;
    Integer level;
    String section;
    Integer profitPerHour;
    Integer profitPerHourDelta;
    Integer price;
    ConditionDto condition;
    Boolean maxLevel;
    Boolean available;
}
