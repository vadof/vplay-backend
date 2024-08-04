package com.vcasino.clicker.service;

import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.entity.Upgrade;
import com.vcasino.clicker.mapper.AccountMapper;
import com.vcasino.clicker.repository.AccountRepository;
import com.vcasino.clicker.utils.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    private final UpgradeService upgradeService;

    public AccountDto createAccount(Long userId) {
        List<Upgrade> upgrades = upgradeService.getInitialUpgrades();

        Account account = Account.builder()
                .userId(userId)
                .level(1)
                .balanceCoins(0L)
                .netWorth(0L)
                .upgrades(upgrades)
                .earnPassivePerHour(calculatePassiveEarnPerHour(upgrades))
                .availableTaps(100)
                .maxTaps(100)
                .earnPerTap(1)
                .tapsRecoverPerSec(3)
                .lastSyncDate(TimeUtil.getCurrentTimestamp())
                .suspiciousActionsNumber(0)
                .frozen(false)
                .build();

        account = accountRepository.saveAndFlush(account);

        return accountMapper.toDto(account);
    }

    private Integer calculatePassiveEarnPerHour(List<Upgrade> upgrades) {
        return upgrades.stream()
                .mapToInt(Upgrade::getProfitPerHour)
                .sum();
    }
}
