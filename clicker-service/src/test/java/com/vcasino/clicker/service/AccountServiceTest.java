package com.vcasino.clicker.service;

import com.vcasino.clicker.config.constants.AccountConstants;
import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.dto.SectionUpgradesDto;
import com.vcasino.clicker.dto.UpgradeUpdateRequest;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.entity.Level;
import com.vcasino.clicker.entity.Upgrade;
import com.vcasino.clicker.exception.AppException;
import com.vcasino.clicker.mapper.AccountMapper;
import com.vcasino.clicker.mapper.AccountMapperImpl;
import com.vcasino.clicker.mapper.ConditionMapper;
import com.vcasino.clicker.mapper.ConditionMapperImpl;
import com.vcasino.clicker.mapper.SectionMapper;
import com.vcasino.clicker.mapper.SectionMapperImpl;
import com.vcasino.clicker.mapper.UpgradeMapper;
import com.vcasino.clicker.mapper.UpgradeMapperImpl;
import com.vcasino.clicker.mock.AccountMocks;
import com.vcasino.clicker.mock.ConditionMocks;
import com.vcasino.clicker.mock.LevelMocks;
import com.vcasino.clicker.mock.UpgradeMocks;
import com.vcasino.clicker.repository.AccountRepository;
import com.vcasino.clicker.utils.TimeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UNIT tests for {@link AccountService}
 */
@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private LevelService levelService;
    @Mock
    private UpgradeService upgradeService;

    @Spy
    private AccountMapper accountMapper = new AccountMapperImpl();

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void iniMapperDependencies() {
        UpgradeMapper upgradeMapper = new UpgradeMapperImpl();
        ConditionMapper conditionMapper = new ConditionMapperImpl();
        SectionMapper sectionMapper = new SectionMapperImpl();

        ReflectionTestUtils.setField(accountMapper, "upgradeMapper", upgradeMapper);
        ReflectionTestUtils.setField(accountMapper, "upgradeService", upgradeService);
        ReflectionTestUtils.setField(upgradeMapper, "sectionMapper", sectionMapper);
        ReflectionTestUtils.setField(upgradeMapper, "conditionMapper", conditionMapper);
    }

    @Test
    @DisplayName("Create Account")
    void createAccount() {
        Upgrade mockedUpgrade = UpgradeMocks.getUpgradeMock("Facebook", 0);
        when(upgradeService.getInitialUpgrades()).thenReturn(List.of(mockedUpgrade));
        when(levelService.getLevelAccordingNetWorth(AccountConstants.BALANCE_COINS)).thenReturn(LevelMocks.getLevelMock());

        SectionUpgradesDto sectionUpgrade = SectionUpgradesDto.builder()
                .order(0)
                .section("Social")
                .upgrades(List.of(UpgradeMocks.getUpgradeDtoMock("Facebook", 0)))
                .build();

        when(upgradeService.getSectionUpgradesList()).thenReturn(List.of(sectionUpgrade));

        Account mockedAccount = AccountMocks.getAccountMock(1L);
        mockedAccount.setUpgrades(List.of(mockedUpgrade));

        when(accountRepository.save(any(Account.class))).thenReturn(mockedAccount);

        AccountDto response = accountService.createAccount(1L);

        verify(accountMapper, times(1)).toDto(any(Account.class));

        assertEquals(mockedAccount.getLevel(), response.getLevel());
        assertEquals(mockedAccount.getBalanceCoins(), response.getBalanceCoins());
        assertEquals(mockedAccount.getNetWorth(), response.getNetWorth());
        assertEquals(mockedAccount.getUpgrades().size(), response.getSectionUpgrades().size());
        assertEquals(1, response.getSectionUpgrades().size());
        assertEquals(mockedUpgrade.getName(), response.getSectionUpgrades().get(0).getUpgrades().get(0).getName());
        assertEquals(mockedUpgrade.getLevel(), response.getSectionUpgrades().get(0).getUpgrades().get(0).getLevel());
        assertTrue(response.getSectionUpgrades().get(0).getUpgrades().get(0).getAvailable());
        assertEquals(mockedAccount.getPassiveEarnPerHour(), response.getPassiveEarnPerHour());
        assertEquals(mockedAccount.getAvailableTaps(), response.getAvailableTaps());
        assertEquals(mockedAccount.getMaxTaps(), response.getMaxTaps());
        assertEquals(mockedAccount.getEarnPerTap(), response.getEarnPerTap());
        assertEquals(mockedAccount.getTapsRecoverPerSec(), response.getTapsRecoverPerSec());
        assertFalse(mockedAccount.getFrozen());
    }

    @Test
    @DisplayName("Mapper sets the available field correctly")
    void accountMapperWorksCorrectly() {
        Upgrade mockedUpgrade = UpgradeMocks.getUpgradeMock("X", 0);
        Upgrade mockedUpgradeWithCondition = UpgradeMocks.getUpgradeMock("Facebook", 0);
        mockedUpgradeWithCondition.setCondition(ConditionMocks.getConditionMock("X", 1));

        List<Upgrade> upgrades = List.of(mockedUpgrade, mockedUpgradeWithCondition);

        SectionUpgradesDto sectionUpgrade = SectionUpgradesDto.builder()
                .order(0)
                .section("Social")
                .upgrades(List.of(
                        UpgradeMocks.getUpgradeDtoMock("Facebook", 0),
                        UpgradeMocks.getUpgradeDtoMock("X", 0))
                )
                .build();

        when(upgradeService.getSectionUpgradesList()).thenReturn(List.of(sectionUpgrade));

        when(upgradeService.getInitialUpgrades()).thenReturn(upgrades);
        when(levelService.getLevelAccordingNetWorth(AccountConstants.BALANCE_COINS)).thenReturn(LevelMocks.getLevelMock());

        Account mockedAccount = AccountMocks.getAccountMock(1L);
        mockedAccount.setUpgrades(upgrades);

        when(accountRepository.save(any(Account.class))).thenReturn(mockedAccount);

        AccountDto response = accountService.createAccount(1L);

        verify(accountMapper, times(1)).toDto(any(Account.class));

        assertEquals(1, response.getSectionUpgrades().size());
        assertEquals(2, response.getSectionUpgrades().get(0).getUpgrades().size());

        assertEquals(upgrades.size(), response.getSectionUpgrades().get(0).getUpgrades().size());

        Map<String, Boolean> responseUpgrades = new HashMap<>();
        response.getSectionUpgrades().get(0).getUpgrades()
                .forEach(u -> responseUpgrades.put(u.getName(), u.getAvailable()));

        assertTrue(responseUpgrades.containsKey("X"));
        assertTrue(responseUpgrades.containsKey("Facebook"));

        assertTrue(responseUpgrades.get("X"), "X should be available, but it is not");
        assertFalse(responseUpgrades.get("Facebook"), "Facebook shouldn't be available, but it is");
    }

    @Test
    @DisplayName("Get account by id")
    void getAccountById() {
        Account account = AccountMocks.getAccountMock(1L);
        account.setId(1L);

        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        Account foundAccount = accountService.getById(account.getId());

        assertEquals(account.getUserId(), foundAccount.getUserId());
        assertEquals(account.getId(), foundAccount.getId());

        assertThrows(AppException.class, () -> accountService.getById(account.getId() + 1));
    }

    @Test
    @DisplayName("Get account by user id")
    void getAccountByUserId() {
        Account account = AccountMocks.getAccountMock(1L);
        account.setId(1L);

        when(accountRepository.findByUserId(account.getId())).thenReturn(Optional.of(account));

        Account foundAccount = accountService.getByUserId(account.getUserId());

        assertEquals(account.getUserId(), foundAccount.getUserId());
        assertEquals(account.getId(), foundAccount.getId());

        assertThrows(AppException.class, () -> accountService.getByUserId(account.getUserId() + 1));
    }

    @Test
    @DisplayName("Add coins")
    void addCoins() {
        Account account = AccountMocks.getAccountMock(1L);

        BigDecimal balance = new BigDecimal(0);
        BigDecimal netWorth = new BigDecimal(0);

        int level = 1;
        long toAdd = 30;
        when(levelService.getLevelAccordingNetWorth(any(Long.class))).thenReturn(new Level(1, "lvl", 0L));

        account.setBalanceCoins(balance);
        account.setNetWorth(netWorth);
        account.setLevel(level);

        accountService.addCoins(account, toAdd);

        assertEquals(balance.add(new BigDecimal(toAdd)), account.getBalanceCoins());
        assertEquals(netWorth.add(new BigDecimal(toAdd)), account.getNetWorth());
        assertEquals(level, account.getLevel());
    }

    @Test
    @DisplayName("Add coins with negative value")
    void addCoinsNegativeValue() {
        Account account = AccountMocks.getAccountMock(1L);

        BigDecimal balance = new BigDecimal(0);
        BigDecimal netWorth = new BigDecimal(0);

        int level = 1;
        long toAdd = -1;

        account.setBalanceCoins(balance);
        account.setNetWorth(netWorth);
        account.setLevel(level);

        assertThrows(IllegalArgumentException.class, () -> accountService.addCoins(account, toAdd));
    }

    @Test
    @DisplayName("Level should be updated with new net worth")
    void addCoinsLevelUpdate() {
        Account account = AccountMocks.getAccountMock(1L);

        BigDecimal balance = new BigDecimal(0);
        BigDecimal netWorth = new BigDecimal(0);

        int level = 1;
        long toAdd = 100;
        when(levelService.getLevelAccordingNetWorth(toAdd)).thenReturn(new Level(2, "lvl2", toAdd));

        account.setBalanceCoins(balance);
        account.setNetWorth(netWorth);
        account.setLevel(level);

        accountService.addCoins(account, toAdd);

        assertEquals(2, account.getLevel());
    }

    @Test
    @DisplayName("When an account is retrieved, balance values must be updated depending on passiveEarn")
    void updateAccountBalance() {
        Account account = AccountMocks.getAccountMock(0L);
        account.setNetWorth(new BigDecimal(0));
        account.setPassiveEarnPerHour(36000);
        account.setLevel(0);

        when(accountRepository.findByUserId(account.getUserId())).thenReturn(Optional.of(account));

        Timestamp lastSync = getTimestamp(0L);
        account.setLastSyncDate(lastSync);

        try (MockedStatic<TimeUtil> ignored = mockStatic(TimeUtil.class)) {
            skipTime(lastSync, 10);

            when(levelService.getLevelAccordingNetWorth(100L)).thenReturn(new Level(2, "d", 100L));

            accountService.getAccount(account.getUserId());

            assertEquals(new BigDecimal(100), account.getBalanceCoins());
            assertEquals(new BigDecimal(100), account.getNetWorth());
            assertEquals(2, account.getLevel());
            assertEquals(account.getLastSyncDate(), Timestamp.from(Instant.ofEpochMilli(lastSync.getNanos() + 10 * 1000)));
        }
    }

    @Test
    @DisplayName("When an account is retrieved, taps must be updated")
    void updateAccountTaps() {
        Account account = AccountMocks.getAccountMock(0L);
        account.setAvailableTaps(0);
        account.setTapsRecoverPerSec(1);

        when(accountRepository.findByUserId(account.getUserId())).thenReturn(Optional.of(account));

        Timestamp lastSync = getTimestamp(0L);
        account.setLastSyncDate(lastSync);

        try (MockedStatic<TimeUtil> ignored = mockStatic(TimeUtil.class)) {
            skipTime(lastSync, 10);

            when(levelService.getLevelAccordingNetWorth(any(Long.class))).thenReturn(LevelMocks.getLevelMock());

            accountService.getAccount(account.getUserId());

            assertEquals(10, account.getAvailableTaps());
            assertEquals(account.getLastSyncDate(), Timestamp.from(Instant.ofEpochMilli(lastSync.getNanos() + 10 * 1000)));
        }
    }

    @Test
    @DisplayName("When an account is retrieved, updated taps value cannot be greater than max taps")
    void updateAccountTapsCannotBeGreaterThanMaxTaps() {
        Account account = AccountMocks.getAccountMock(0L);

        account.setAvailableTaps(0);
        account.setMaxTaps(5);
        account.setTapsRecoverPerSec(1);

        Timestamp lastSync = getTimestamp(0L);
        account.setLastSyncDate(lastSync);

        try (MockedStatic<TimeUtil> ignored = mockStatic(TimeUtil.class)) {
            skipTime(lastSync, 10);

            when(levelService.getLevelAccordingNetWorth(any(Long.class))).thenReturn(LevelMocks.getLevelMock());

            accountService.updateAccount(account);

            assertEquals(5, account.getAvailableTaps());
            assertEquals(account.getLastSyncDate(), Timestamp.from(Instant.ofEpochMilli(lastSync.getNanos() + 10 * 1000)));
        }
    }

    @Test
    @DisplayName("If account is frozen exception is thrown")
    void handleFrozenAccount() {
        Account account = AccountMocks.getAccountMock(1L);
        account.setId(1L);

        account.setFrozen(true);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(account));

        assertThrows(AppException.class, () -> accountService.getById(1L));
        assertThrows(AppException.class, () -> accountService.getByUserId(1L));
    }

    @Test
    @DisplayName("Update upgrade")
    void updateUpgrade() {
        Account account = AccountMocks.getAccountMock(1L);
        when(accountRepository.findByUserId(account.getUserId())).thenReturn(Optional.of(account));

        Upgrade toUpdate = UpgradeMocks.getUpgradeMock("1", 0);
        List<Upgrade> upgrades = new ArrayList<>();
        upgrades.add(toUpdate);
        upgrades.add(UpgradeMocks.getUpgradeMock("2", 0));
        account.setUpgrades(upgrades);

        account.setBalanceCoins(new BigDecimal(1000));

        Upgrade expectedNew = UpgradeMocks.getUpgradeMock("1", 1, 100);

        when(upgradeService.findUpgradeInAccount(account, toUpdate.getName(), toUpdate.getLevel())).thenReturn(toUpdate);
        when(upgradeService.findUpgrade(toUpdate.getName(), toUpdate.getLevel() + 1)).thenReturn(expectedNew);
        when(upgradeService.calculatePassiveEarnPerHour(any())).thenReturn(expectedNew.getProfitPerHour() + upgrades.get(1).getProfitPerHour());

        UpgradeUpdateRequest upgradeUpdateRequest = new UpgradeUpdateRequest(toUpdate.getName(), toUpdate.getLevel());
        accountService.updateUpgrade(upgradeUpdateRequest, account.getUserId());

//        verify(accountRepository, times(1)).save(accountArgumentCaptor.capture());
//        Account updatedAccount = accountArgumentCaptor.getValue();

        List<Upgrade> updatedUpgrades = account.getUpgrades();
        assertEquals(2, updatedUpgrades.size());
        assertEquals(100, account.getPassiveEarnPerHour());
        assertEquals(new BigDecimal(0), account.getBalanceCoins());
        assertTrue(updatedUpgrades.stream().anyMatch(u -> u.getName().equals(expectedNew.getName())
                && u.getLevel().equals(expectedNew.getLevel())));
        assertFalse(updatedUpgrades.stream().anyMatch(u -> u.getName().equals(toUpdate.getName())
                && u.getLevel().equals(toUpdate.getLevel())));
    }

    @Test
    @DisplayName("Upgrade is already max level")
    void upgradeNotInAccount() {
        Account account = AccountMocks.getAccountMock(1L);
        when(accountRepository.findByUserId(account.getUserId())).thenReturn(Optional.of(account));

        Upgrade toUpdate = UpgradeMocks.getUpgradeMock("1", 10);
        toUpdate.setMaxLevel(true);
        List<Upgrade> upgrades = new ArrayList<>();
        upgrades.add(toUpdate);
        upgrades.add(UpgradeMocks.getUpgradeMock("2", 0));
        account.setUpgrades(upgrades);

        when(upgradeService.findUpgradeInAccount(account, toUpdate.getName(), toUpdate.getLevel())).thenReturn(toUpdate);
        UpgradeUpdateRequest upgradeUpdateRequest = new UpgradeUpdateRequest(toUpdate.getName(), toUpdate.getLevel());

        assertThrows(AppException.class, () -> accountService.updateUpgrade(upgradeUpdateRequest, account.getUserId()));
    }

    @Test
    @DisplayName("Update upgrade not enough money")
    void updateUpgradeNotEnoughMoney() {
        Account account = AccountMocks.getAccountMock(1L);
        when(accountRepository.findByUserId(account.getUserId())).thenReturn(Optional.of(account));

        Upgrade toUpdate = UpgradeMocks.getUpgradeMock("1", 0);
        toUpdate.setPrice(10000);
        List<Upgrade> upgrades = new ArrayList<>();
        upgrades.add(toUpdate);
        upgrades.add(UpgradeMocks.getUpgradeMock("2", 0));
        account.setUpgrades(upgrades);

        account.setBalanceCoins(new BigDecimal(toUpdate.getPrice() - 1));

        when(upgradeService.findUpgradeInAccount(account, toUpdate.getName(), toUpdate.getLevel())).thenReturn(toUpdate);

        UpgradeUpdateRequest upgradeUpdateRequest = new UpgradeUpdateRequest(toUpdate.getName(), toUpdate.getLevel());
        assertThrows(AppException.class, () -> accountService.updateUpgrade(upgradeUpdateRequest, account.getUserId()));
    }

    @Test
    @DisplayName("Update upgrade condition not completed")
    void updateUpgradeConditionNotCompleted() {
        Account account = AccountMocks.getAccountMock(1L);
        when(accountRepository.findByUserId(account.getUserId())).thenReturn(Optional.of(account));

        List<Upgrade> upgrades = new ArrayList<>();
        upgrades.add(UpgradeMocks.getUpgradeMock("1", 0));
        Upgrade toUpdate = UpgradeMocks.getUpgradeMock("2", 0);
        toUpdate.setCondition(ConditionMocks.getConditionMock("1", 1));
        upgrades.add(toUpdate);
        account.setUpgrades(upgrades);

        account.setBalanceCoins(new BigDecimal(1000));

        when(upgradeService.findUpgradeInAccount(account, toUpdate.getName(), toUpdate.getLevel())).thenReturn(toUpdate);

        UpgradeUpdateRequest upgradeUpdateRequest = new UpgradeUpdateRequest(toUpdate.getName(), toUpdate.getLevel());
        assertThrows(AppException.class, () -> accountService.updateUpgrade(upgradeUpdateRequest, account.getUserId()));
    }

    private void skipTime(Timestamp start, long seconds) {
        Timestamp now = Timestamp.from(Instant.ofEpochMilli(start.getTime() + seconds * 1000));

        when(TimeUtil.getCurrentTimestamp()).thenReturn(now);
        when(TimeUtil.getDifferenceInSeconds(start, now)).thenReturn(seconds);
    }

    private Timestamp getTimestamp(Long value) {
        return Timestamp.from(Instant.ofEpochMilli(value));
    }
}
