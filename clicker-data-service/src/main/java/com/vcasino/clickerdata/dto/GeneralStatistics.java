package com.vcasino.clickerdata.dto;

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
public class GeneralStatistics {
    Long activeUsersToday;
    Long clicksToday;
    Long clicksPerUser;
    Long suspiciousActivityCount;
    Long frozenAccounts;
    Long streaksTakenToday;
    Long totalNetWorth;
    Long totalUpgradesPurchased;
    ChartData<String, Integer> activeUsersChart;
    ChartData<String, Long> totalClicksChart;
    ChartData<Integer, Double> levelPercentageChart;
}
