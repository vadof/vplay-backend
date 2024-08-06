package com.vcasino.clicker.mock;

import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.utils.TimeUtil;

import java.util.ArrayList;

public class AccountMocks {
    public static Account getAccountMock(Long userId) {
        return Account.builder()
                .id(1L)
                .userId(userId)
                .level(0)
                .balanceCoins(0L)
                .netWorth(0L)
                .upgrades(new ArrayList<>())
                .earnPassivePerHour(0)
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
                .balanceCoins(0L)
                .netWorth(0L)
                .upgrades(new ArrayList<>())
                .earnPassivePerHour(0)
                .availableTaps(100)
                .maxTaps(100)
                .earnPerTap(1)
                .tapsRecoverPerSec(3)
                .build();
    }
}
