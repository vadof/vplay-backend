package com.vcasino.clickerdata.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountInformation {
    Long id;
    String username;
    Integer level;
    Double balanceCoins;
    BigDecimal netWorth;
    Integer passiveEarnPerHour;
    String lastSyncDate;
    Integer suspiciousActionsNumber;
    Boolean frozen;
    Integer purchasedUpgrades;
    Integer streak;
    String lastReceivedStreakDay;
    Integer completedTasks;
    Long totalClicks;
    Integer bestClickDayAmount;
    String bestClickDayDate;
    ChartData<String, Integer> clicksChart;
}
