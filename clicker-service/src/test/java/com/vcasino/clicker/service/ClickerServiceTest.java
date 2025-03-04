package com.vcasino.clicker.service;

import com.vcasino.clicker.config.constants.ClickerConstants;
import com.vcasino.clicker.dto.Tap;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.mock.AccountMocks;
import com.vcasino.clicker.utils.TimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * UNIT tests for {@link ClickerService}
 */
@ExtendWith(MockitoExtension.class)
public class ClickerServiceTest {

    @Mock
    private AccountService accountService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    ClickerService clickerService;

    @Test
    @DisplayName("Tap")
    void tap() {
        Account account = AccountMocks.getAccountMock(1L);

        long lastAccountSyncTime = 0;
        long currentUnixTime = 10;
        long tapTime = 9;

        account.getLevel().setEarnPerTap(1);
        account.setBalanceCoins(new BigDecimal(0));
        account.setNetWorth(new BigDecimal(0));
        account.setLastSyncDate(getTimestamp(lastAccountSyncTime));
        account.setAvailableTaps(100);
        account.getLevel().setMaxTaps(100);

        when(accountService.getById(1L)).thenReturn(account);
        when(accountService.calculatePassiveEarn(any(), any())).thenReturn(new BigDecimal(0));

        try (MockedStatic<TimeUtil> ignored = mockStatic(TimeUtil.class)) {
            Tap tap = new Tap(100, 0, tapTime);
            mockTime(currentUnixTime, lastAccountSyncTime, tapTime);

            clickerService.tap(tap, 1L);

            assertEquals(0, account.getAvailableTaps());
            assertEquals(getTimestamp(tapTime), account.getLastSyncDate());
        }
    }

    @Test
    @DisplayName("When lastSyncTime is greater than tap's timestamp result is ignored")
    void tapWithDelayedTimestamp() {
        Account account = AccountMocks.getAccountMock(1L);

        long lastAccountSyncTime = 10;
        long currentUnixTime = 10;
        long tapTime = 9;

        account.getLevel().setEarnPerTap(1);
        account.setLastSyncDate(getTimestamp(lastAccountSyncTime));
        account.setAvailableTaps(100);
        account.getLevel().setMaxTaps(100);

        when(accountService.getById(1L)).thenReturn(account);

        try (MockedStatic<TimeUtil> ignored = mockStatic(TimeUtil.class)) {
            Tap tap = new Tap(100, 0, tapTime);
            mockTime(currentUnixTime, lastAccountSyncTime, tapTime);

            clickerService.tap(tap, 1L);

            assertEquals(100, account.getAvailableTaps());
            assertEquals(getTimestamp(lastAccountSyncTime), account.getLastSyncDate());
        }
    }

    @Test
    @DisplayName("When tap's timestamp is greater than current timestamp - suspicious behaviour")
    void tapWithFutureTimestamp() {
        Account account = AccountMocks.getAccountMock(1L);

        long lastAccountSyncTime = 0;
        long currentUnixTime = 10;
        long tapTime = 11;

        account.getLevel().setEarnPerTap(1);
        account.setLastSyncDate(getTimestamp(lastAccountSyncTime));
        account.setAvailableTaps(100);
        account.getLevel().setMaxTaps(100);
        account.setSuspiciousActionsNumber(0);
        account.setFrozen(false);

        when(accountService.getById(1L)).thenReturn(account);

        try (MockedStatic<TimeUtil> ignored = mockStatic(TimeUtil.class)) {
            Tap tap = new Tap(100, 0, tapTime);
            mockTime(currentUnixTime, lastAccountSyncTime, tapTime);

            for (int i = 1; i <= ClickerConstants.MAX_SUSPICIOUS_ACTIONS; i++) {
                clickerService.tap(tap, 1L);
                assertEquals(100, account.getAvailableTaps());
                assertEquals(getTimestamp(lastAccountSyncTime), account.getLastSyncDate());
                assertEquals(i, account.getSuspiciousActionsNumber());
            }

            assertTrue(account.getFrozen());
        }
    }

    @Test
    @DisplayName("When availableTaps is greater than max taps - suspicious behaviour")
    void availableTapsIsGreaterThanMaxTaps() {
        Account account = AccountMocks.getAccountMock(1L);

        long lastAccountSyncTime = 0;
        long currentUnixTime = 10;
        long tapTime = 10;

        account.getLevel().setEarnPerTap(1);
        account.setLastSyncDate(getTimestamp(lastAccountSyncTime));
        account.setAvailableTaps(100);
        account.getLevel().setMaxTaps(100);
        account.setSuspiciousActionsNumber(0);
        account.setFrozen(false);

        when(accountService.getById(1L)).thenReturn(account);

        try (MockedStatic<TimeUtil> ignored = mockStatic(TimeUtil.class)) {
            Tap tap = new Tap(30, 101, tapTime);
            mockTime(currentUnixTime, lastAccountSyncTime, tapTime);

            for (int i = 1; i <= ClickerConstants.MAX_SUSPICIOUS_ACTIONS; i++) {
                clickerService.tap(tap, 1L);
                assertEquals(100, account.getAvailableTaps());
                assertEquals(getTimestamp(lastAccountSyncTime), account.getLastSyncDate());
                assertEquals(i, account.getSuspiciousActionsNumber());
            }

            assertTrue(account.getFrozen());
        }
    }

    @Test
    @DisplayName("When tapAmount is greater than can be taped over time - suspicious behaviour")
    void tapedMoreThanPossible() {
        Account account = AccountMocks.getAccountMock(1L);

        long lastAccountSyncTime = 0;
        long currentUnixTime = 10;
        long tapTime = 10;

        account.getLevel().setEarnPerTap(1);
        account.setLastSyncDate(getTimestamp(lastAccountSyncTime));
        account.setAvailableTaps(100);
        account.getLevel().setMaxTaps(100);
        account.setSuspiciousActionsNumber(0);
        account.setFrozen(false);

        when(accountService.getById(1L)).thenReturn(account);

        try (MockedStatic<TimeUtil> ignored = mockStatic(TimeUtil.class)) {
            Tap tap = new Tap(200, 100, tapTime);
            mockTime(currentUnixTime, lastAccountSyncTime, tapTime);

            for (int i = 1; i <= ClickerConstants.MAX_SUSPICIOUS_ACTIONS; i++) {
                clickerService.tap(tap, 1L);
                assertEquals(100, account.getAvailableTaps());
                assertEquals(getTimestamp(lastAccountSyncTime), account.getLastSyncDate());
                assertEquals(i, account.getSuspiciousActionsNumber());
            }

            assertTrue(account.getFrozen());
        }
    }

    private void mockTime(long currentUnixTime, long lastAccountSyncTime, long tapTime) {
        when(TimeUtil.getCurrentUnixTime()).thenReturn(currentUnixTime);
        when(TimeUtil.toUnixTime(getTimestamp(lastAccountSyncTime))).thenReturn(lastAccountSyncTime);
        when(TimeUtil.getDifferenceInSeconds(lastAccountSyncTime, tapTime)).thenReturn(tapTime - lastAccountSyncTime);
        when(TimeUtil.toTimestamp(tapTime)).thenReturn(getTimestamp(tapTime));
    }

    private Timestamp getTimestamp(Long value) {
        return Timestamp.from(Instant.ofEpochMilli(value * 1000));
    }

}
