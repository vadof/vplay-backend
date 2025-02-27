package com.vcasino.clicker.service;

import com.vcasino.clicker.config.constants.AccountConstants;
import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.dto.AccountResponse;
import com.vcasino.clicker.dto.BuyUpgradeRequest;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.entity.Condition;
import com.vcasino.clicker.entity.Level;
import com.vcasino.clicker.entity.Upgrade;
import com.vcasino.clicker.exception.AppException;
import com.vcasino.clicker.mapper.AccountMapper;
import com.vcasino.clicker.repository.AccountRepository;
import com.vcasino.clicker.utils.TimeUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Collections;


@Service
@AllArgsConstructor
@Slf4j
public class AccountService {

    @PersistenceContext
    private EntityManager entityManager;

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    private final LevelService levelService;
    private final UpgradeService upgradeService;

    public void createAccount(Long id, String username, String invitedBy) {
        Account account = buildAccount(id, username);
        setInvitedBy(account, invitedBy);
        account = save(account);
        log.info("Account#{} saved to database", account.getId());
    }

    private Account buildAccount(Long id, String username) {
        Level level = levelService.getLevelAccordingNetWorth(AccountConstants.BALANCE_COINS);

        return Account.builder()
                .id(id)
                .username(username)
                .level(level)
                .balanceCoins(AccountConstants.BALANCE_COINS)
                .netWorth(AccountConstants.BALANCE_COINS)
                .upgrades(Collections.emptyList())
                .passiveEarnPerHour(0)
                .availableTaps(level.getMaxTaps())
                .lastSyncDate(TimeUtil.getCurrentTimestamp())
                .suspiciousActionsNumber(0)
                .frozen(false)
                .build();
    }

    // TODO
    public void setInvitedBy(Account account, String inivitedBy) {

    }

    public Account getById(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(() ->
                new AppException("Account#" + id + " not found", HttpStatus.NOT_FOUND));

        handleFrozenAccount(account);

        return account;
    }

    @Transactional
    public AccountResponse buyUpgrade(BuyUpgradeRequest request, Long accountId) {
        Account account = getById(accountId);
        updateAccount(account, false);

        Upgrade upgrade = upgradeService.findUpgradeInAccount(account, request.getUpgradeName())
                .orElseGet(() -> upgradeService.findUpgrade(request.getUpgradeName(), 0));

        if (upgrade.getMaxLevel()) {
            throw new AppException("The maximum level has already been reached", HttpStatus.BAD_REQUEST);
        }

        validateMoney(account, upgrade);
        validateCondition(account, upgrade.getCondition());

        Upgrade updatedUpgrade = upgradeService.findUpgrade(upgrade.getName(), upgrade.getLevel() + 1);

        int success;
        if (upgrade.getLevel() == 0) {
            success = accountRepository.addUpgrade(account.getId(), upgrade.getName(), updatedUpgrade.getLevel());
        } else {
            success = accountRepository.updateUpgradeLevel(account.getId(), upgrade.getName(), updatedUpgrade.getLevel());
        }

        if (success != 1) {
            log.error("Error add/update upgrade Account#{}, Upgrade: {}-{}",
                    account.getId(), updatedUpgrade.getName(), updatedUpgrade.getLevel());
            throw new AppException(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        account.setBalanceCoins(account.getBalanceCoins().subtract(new BigDecimal(upgrade.getPrice())));

        int additionalProfit = updatedUpgrade.getProfitPerHour() - upgrade.getProfitPerHour();
        account.setPassiveEarnPerHour(account.getPassiveEarnPerHour() + additionalProfit);

        account = save(account);

        // Avoid extra Account updates
        entityManager.flush();
        entityManager.clear();

        // Required to display bought upgrade in response
        if (upgrade.getLevel() > 0) {
            account.getUpgrades().remove(upgrade);
        }
        account.getUpgrades().add(updatedUpgrade);

        return toAccountResponse(account);
    }

    private void validateMoney(Account account, Upgrade upgrade) {
        boolean enoughMoney = new BigDecimal(upgrade.getPrice()).compareTo(account.getBalanceCoins()) < 1;
        if (!enoughMoney) {
            log.warn("User cannot buy upgrade - not enough money. Balance: {}, Price: {}",
                    account.getBalanceCoins(), upgrade.getPrice());
            throw new AppException("Not enough money to buy upgrade", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateCondition(Account account, Condition condition) {
        boolean conditionCompleted = condition == null || account.getUpgrades().stream()
                .anyMatch(u -> u.getName().equals(condition.getUpgradeName()) &&
                        u.getLevel() >= condition.getLevel());
        if (!conditionCompleted) {
            log.warn("User cannot buy upgrade - condition not completed.");
            throw new AppException("Condition not completed", HttpStatus.BAD_REQUEST);
        }
    }

    public void handleFrozenAccount(Account account) {
        if (account.getFrozen()) {
            throw new AppException(AccountConstants.ACCOUNT_FROZEN_DISPLAY_MESSAGE, HttpStatus.LOCKED);
        }
    }

    public Account save(Account account) {
        account = accountRepository.save(account);
        return account;
    }

    public AccountResponse toAccountResponse(Account account) {
        return accountMapper.toResponse(account);
    }

    public AccountDto toDto(Account account) {
        return accountMapper.toDto(account);
    }

    public void addCoins(Account account, Integer amount) {
        addCoins(account, new BigDecimal(amount));
    }

    public void addCoins(Account account, Long amount) {
        addCoins(account, new BigDecimal(amount));
    }

    public void addCoins(Account account, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        updateAccountBalance(account, amount);
    }

    public AccountResponse getAccount(Long id) {
        Account account = getById(id);
        updateAccount(account);
        return toAccountResponse(account);
    }

    public void updateAccount(Account account) {
        updateAccount(account, true);
    }

    private void updateAccount(Account account, Boolean save) {
        Timestamp lastSync = account.getLastSyncDate();
        Timestamp now = TimeUtil.getCurrentTimestamp();
        Long secondsDiff = TimeUtil.getDifferenceInSeconds(lastSync, now);

        if (secondsDiff != 0) {
            BigDecimal passiveEarn = calculatePassiveEarn(account.getPassiveEarnPerHour(), secondsDiff);
            updateAccountBalance(account, passiveEarn);
            updateAccountTaps(account, secondsDiff);
            account.setLastSyncDate(now);
        }

        if (save) {
            save(account);
        }
    }

    private void updateAccountBalance(Account account, BigDecimal earned) {
        account.setBalanceCoins(account.getBalanceCoins().add(earned));
        account.setNetWorth(account.getNetWorth().add(earned));
        Level level = levelService.getLevelAccordingNetWorth(account.getNetWorth().longValue());
        if (level.getValue() > account.getLevel().getValue()) {
            account.setLevel(level);
        }
    }

    private void updateAccountTaps(Account account, Long differenceInSecond) {
        long newTapsValue = differenceInSecond * account.getLevel().getTapsRecoverPerSec() + account.getAvailableTaps();
        if (newTapsValue > Integer.MAX_VALUE) {
            account.setAvailableTaps(account.getLevel().getMaxTaps());
        } else {
            account.setAvailableTaps(Math.min((int) newTapsValue, account.getLevel().getMaxTaps()));
        }
    }

    public BigDecimal calculatePassiveEarn(Integer passiveEarnPerHour, Long differenceInSeconds) {
        double earned = getPassiveEarnPerSecond(passiveEarnPerHour) * differenceInSeconds;
        return new BigDecimal(earned).setScale(3, RoundingMode.HALF_UP);
    }

    public Double getPassiveEarnPerSecond(Integer passiveEarnPerHour) {
        return passiveEarnPerHour / 3600d;
    }
}
