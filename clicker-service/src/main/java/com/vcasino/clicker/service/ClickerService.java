package com.vcasino.clicker.service;

import com.vcasino.clicker.config.constants.ClickerConstants;
import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.dto.Tap;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.utils.SuspiciousTapAction;
import com.vcasino.clicker.utils.TimeUtil;
import com.vcasino.commonkafka.enums.Topic;
import com.vcasino.commonkafka.event.ClickEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
@Slf4j
public class ClickerService {

    private final AccountService accountService;
    private final AsyncKafkaPublisher kafkaPublisher;

    @Transactional
    public AccountDto tap(Tap tap, Long accountId) {
        Account account = accountService.getById(accountId);

        if (tap.getAvailableTaps() > account.getLevel().getMaxTaps()) {
            String message = "Available taps is more than max taps";
            return handleSuspiciousBehaviour(new SuspiciousTapAction(account, tap, message));
        }

        Long tapTimestamp = tap.getTimestamp();
        Long currentTimestamp = TimeUtil.getCurrentUnixTime();

        if (tapTimestamp > currentTimestamp && tapTimestamp - currentTimestamp > 3) {
            String message = "Does the user live in the future???";
            return handleSuspiciousBehaviour(new SuspiciousTapAction(account, tap, message));
        }

        long lastSync = TimeUtil.toUnixTime(account.getLastSyncDate());

        if (lastSync >= tapTimestamp) {
            log.warn("Late request received lastSync={}, tapTimestamp={}", lastSync, tapTimestamp);
            return accountService.toDto(account);
        }

        long diff = TimeUtil.getDifferenceInSeconds(lastSync, tapTimestamp);
        long recoveredOverTime = account.getLevel().getTapsRecoverPerSec() * (diff + ClickerConstants.UNCERTAINTY_IN_SECONDS);
        long canBeTappedOverTime = account.getAvailableTaps() + recoveredOverTime - tap.getAvailableTaps();

        if (tap.getAmount() > canBeTappedOverTime) {
            String message = "Tap amount is more than can be tapped over time (%s)".formatted(canBeTappedOverTime);
            return handleSuspiciousBehaviour(new SuspiciousTapAction(account, tap, message));
        }

        BigDecimal passiveEarn = accountService.calculatePassiveEarn(account.getPassiveEarnPerHour(), diff);
        BigDecimal toAdd = passiveEarn.add(new BigDecimal(tap.getAmount() * account.getLevel().getEarnPerTap()));
        accountService.addCoins(account, toAdd);

        account.setAvailableTaps(tap.getAvailableTaps());
        account.setLastSyncDate(TimeUtil.toTimestamp(tapTimestamp));

        account = accountService.save(account);

        kafkaPublisher.send(Topic.CLICK_EVENTS, new ClickEvent(accountId, tap.getAmount()), false);

        return accountService.toDto(account);
    }

    private AccountDto handleSuspiciousBehaviour(SuspiciousTapAction suspiciousAction) {
        log.warn("Suspicious behaviour: {}", suspiciousAction);
        Account account = suspiciousAction.getAccount();

        int suspiciousActionsNumber = account.getSuspiciousActionsNumber() + 1;
        if (suspiciousActionsNumber == ClickerConstants.MAX_SUSPICIOUS_ACTIONS) {
            account.setFrozen(true);
            log.info("Account#{} frozen due to large amount of suspicious activity", account.getId());
        }

        account.setSuspiciousActionsNumber(suspiciousActionsNumber);
        accountService.updateAccount(account);

        return accountService.toDto(account);
    }

}
