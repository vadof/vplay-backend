package com.vcasino.clicker.service;

import com.vcasino.clicker.config.constants.AccountConstants;
import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.dto.BuyUpgradeRequest;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.entity.Condition;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.List;


@Service
@AllArgsConstructor
@Slf4j
public class AccountService {

    private final RedisService redisService;
    private static final String ACCOUNT_KEY_PREFIX = "account:";

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    private final LevelService levelService;
    private final UpgradeService upgradeService;

    public AccountDto createAccount(Long id, String username, String invitedBy) {
        Account account = buildAccount(id, username);
        setInvitedBy(account, invitedBy);
        account = save(account);
        log.info("Account#{} saved to database", account.getId());
        return toDto(account);
    }

    private Account buildAccount(Long id, String username) {
        List<Upgrade> upgrades = upgradeService.getInitialUpgrades();
        Level level = levelService.getLevelAccordingNetWorth(AccountConstants.BALANCE_COINS);

        return Account.builder()
                .id(id)
                .username(username)
                .level(level)
                .balanceCoins(AccountConstants.BALANCE_COINS)
                .netWorth(AccountConstants.BALANCE_COINS)
                .upgrades(upgrades)
                .passiveEarnPerHour(upgradeService.calculatePassiveEarnPerHour(upgrades))
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
        String key = ACCOUNT_KEY_PREFIX + id;

        Account account = redisService.get(key, Account.class);

        if (account != null) {
            return account;
        } else {
            // TODO connect with user service and check for user
            account = accountRepository.findById(id).orElseThrow(() ->
                    new AppException("Account#" + id + " not found", HttpStatus.NOT_FOUND));
        }

        handleFrozenAccount(account);

        return account;
    }

    public AccountDto buyUpgrade(BuyUpgradeRequest request, Long accountId) {
        Account account = getById(accountId);
        updateAccount(account, false);

        Upgrade upgrade = upgradeService.findUpgradeInAccount(account, request.getUpgradeName(), request.getUpgradeLevel());

        if (upgrade.getMaxLevel()) {
            throw new AppException("The maximum level has already been reached", HttpStatus.BAD_REQUEST);
        }
        validateMoney(account, upgrade);
        validateCondition(account, upgrade.getCondition());

        Upgrade updatedUpgrade = upgradeService.findUpgrade(upgrade.getName(), upgrade.getLevel() + 1);
        account.getUpgrades().remove(upgrade);
        account.getUpgrades().add(updatedUpgrade);
        account.setBalanceCoins(account.getBalanceCoins().subtract(new BigDecimal(upgrade.getPrice())));
        account.setPassiveEarnPerHour(upgradeService.calculatePassiveEarnPerHour(account.getUpgrades()));

        return toDto(save(account));
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
        redisService.save(ACCOUNT_KEY_PREFIX + account.getId(), account, 5);
        return account;
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
        log.info("Add {} coins to Account#{}", amount, account.getId());
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        updateAccountBalance(account, amount);
    }

    public AccountDto getAccount(Long id) {
        Account account = getById(id);
        updateAccount(account);
        return toDto(account);
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
