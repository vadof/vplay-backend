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
public class BoostDto {
    String name;
    Integer level;
    Integer currentValue;
    ConditionDto condition;
    String priceToUpgrade;
    Boolean maxLevel;
}
