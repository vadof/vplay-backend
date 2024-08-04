package com.vcasino.clicker.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountDto {
    Integer level;
    Long netWorth;
    Long balanceCoins;
    Integer availableTaps;
    Integer maxTaps;
    Integer earnPerTap;
    Integer tapsRecoverPerSec;
    Integer earnPassivePerHour;
    List<UpgradeDto> upgrades;
}
