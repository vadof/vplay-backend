package com.vcasino.clicker.service;

import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.entity.Level;
import com.vcasino.clicker.entity.Upgrade;
import com.vcasino.clicker.exception.AppException;
import com.vcasino.clicker.mapper.AccountMapper;
import com.vcasino.clicker.repository.AccountRepository;
import com.vcasino.clicker.utils.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    private final LevelService levelService;
    private final UpgradeService upgradeService;

    public AccountDto createAccount(Long userId) {
        Account account = buildAccount(userId);
        account = accountRepository.saveAndFlush(account);
        log.info("Account#{} saved to database", account.getId());
        return accountMapper.toDto(account);
    }

    private Account buildAccount(Long userId) {
        List<Upgrade> upgrades = upgradeService.getInitialUpgrades();
        Level level = levelService.getLevelAccordingNetWorth(0L);

        return Account.builder()
                .userId(userId)
                .level(level.getValue())
                .balanceCoins(0L)
                .netWorth(0L)
                .upgrades(upgrades)
                .earnPassivePerHour(upgradeService.calculatePassiveEarnPerHour(upgrades))
                .availableTaps(100)
                .maxTaps(100)
                .earnPerTap(1)
                .tapsRecoverPerSec(3)
                .lastSyncDate(TimeUtil.getCurrentTimestamp())
                .suspiciousActionsNumber(0)
                .frozen(false)
                .build();
    }

    public Account getById(Long id) {
        return accountRepository.findById(id).orElseThrow(()
                -> new AppException("User#" + id + " not found", HttpStatus.NOT_FOUND));
    }

    public void addCoins(Account account, Long amount) {
        log.info("Add {} coins to Account#{}", amount, account.getId());
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        account.setBalanceCoins(account.getBalanceCoins() + amount);
        account.setNetWorth(account.getNetWorth() + amount);
        Level level = levelService.getLevelAccordingNetWorth(account.getNetWorth());
        account.setLevel(level.getValue());
    }
}
