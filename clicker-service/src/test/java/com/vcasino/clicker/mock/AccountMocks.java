package com.vcasino.clicker.mock;

import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.utils.TimeUtil;

import java.math.BigDecimal;
import java.util.ArrayList;

public class AccountMocks {
    public static Account getAccountMock(Long userId) {
        return Account.builder()
                .id(1L)
                .userId(userId)
                .level(0)
                .balanceCoins(new BigDecimal(0))
                .netWorth(new BigDecimal(0))
                .upgrades(new ArrayList<>())
                .passiveEarnPerHour(0)
                .availableTaps(100)
                .maxTaps(100)
                .earnPerTap(1)
                .tapsRecoverPerSec(3)
                .lastSyncDate(TimeUtil.getCurrentTimestamp())
                .suspiciousActionsNumber(0)
                .frozen(false)
                .build();
    }

    public static AccountDto getAccountDtoMock() {
        return AccountDto.builder()
                .level(0)
                .balanceCoins(new BigDecimal(0))
                .netWorth(new BigDecimal(0))
                .upgrades(new ArrayList<>())
                .passiveEarnPerHour(0)
                .availableTaps(100)
                .maxTaps(100)
                .earnPerTap(1)
                .tapsRecoverPerSec(3)
                .build();
    }
}
