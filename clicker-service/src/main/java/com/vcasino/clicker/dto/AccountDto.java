package com.vcasino.clicker.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountDto {
    Integer level;
    BigDecimal netWorth;
    BigDecimal balanceCoins;
    Integer availableTaps;
    Integer maxTaps;
    Integer earnPerTap;
    Integer tapsRecoverPerSec;
    Integer passiveEarnPerHour;
    Double passiveEarnPerSec;
    List<SectionUpgradesDto> sectionUpgrades;
}
