package com.vcasino.clicker.service;

import com.vcasino.clicker.config.constants.AccountConstants;
import com.vcasino.clicker.dto.AccountResponse;
import com.vcasino.clicker.dto.BuyUpgradeRequest;
import com.vcasino.clicker.dto.SectionUpgradesDto;
import com.vcasino.clicker.dto.UpgradeDto;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.entity.Level;
import com.vcasino.clicker.entity.Upgrade;
import com.vcasino.clicker.exception.AppException;
import com.vcasino.clicker.mapper.AccountMapper;
import com.vcasino.clicker.mapper.AccountMapperImpl;
import com.vcasino.clicker.mapper.ConditionMapper;
import com.vcasino.clicker.mapper.ConditionMapperImpl;
import com.vcasino.clicker.mapper.UpgradeMapper;
import com.vcasino.clicker.mapper.UpgradeMapperImpl;
import com.vcasino.clicker.mock.AccountMocks;
import com.vcasino.clicker.mock.ConditionMocks;
import com.vcasino.clicker.mock.LevelMocks;
import com.vcasino.clicker.mock.UpgradeMocks;
import com.vcasino.clicker.repository.AccountRepository;
import com.vcasino.clicker.utils.TimeUtil;
import jakarta.persistence.EntityManager;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
    @Mock
    private EntityManager entityManager;

    @Spy
    private AccountMapper accountMapper = new AccountMapperImpl();

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void iniMapperDependencies() {
        UpgradeMapper upgradeMapper = new UpgradeMapperImpl();
        ConditionMapper conditionMapper = new ConditionMapperImpl();

        ReflectionTestUtils.setField(accountMapper, "upgradeMapper", upgradeMapper);
        ReflectionTestUtils.setField(accountMapper, "upgradeService", upgradeService);
        ReflectionTestUtils.setField(upgradeMapper, "conditionMapper", conditionMapper);
    }

    @Test
    @DisplayName("Create Account")
    void createAccount() {
        Account mockedAccount = AccountMocks.getAccountMock(1L);

        when(levelService.getLevelAccordingNetWorth(AccountConstants.BALANCE_COINS)).thenReturn(LevelMocks.getLevelMock(1));
        when(accountRepository.save(any(Account.class))).thenReturn(mockedAccount);

        accountService.createAccount(1L, mockedAccount.getUsername(), null);

        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("Mapper sets the available field correctly")
    void accountMapperWorksCorrectly() {
        Upgrade userUpgrade = UpgradeMocks.getUpgradeMock("X", 1);
        Upgrade userUpgradeWithCondition = UpgradeMocks.getUpgradeMock("Facebook", 1);
        userUpgradeWithCondition.setCondition(ConditionMocks.getConditionMock("X", 2));

        List<Upgrade> userUpgrades = List.of(userUpgrade, userUpgradeWithCondition);

        Account mockedAccount = AccountMocks.getAccountMock(1L);
        mockedAccount.setUpgrades(userUpgrades);

        UpgradeDto x = UpgradeMocks.getUpgradeDtoMock("X", 0);
        UpgradeDto facebook = UpgradeMocks.getUpgradeDtoMock("Facebook", 0);
        UpgradeDto youtube = UpgradeMocks.getUpgradeDtoMock("YouTube", 0);

        SectionUpgradesDto sectionUpgrades = SectionUpgradesDto.builder()
                .order(0)
                .section("Social")
                .upgrades(List.of(x, facebook, youtube))
                .build();

        when(upgradeService.getSectionUpgradesList()).thenReturn(List.of(sectionUpgrades));
        when(upgradeService.getAllUpgradesIncludingMissing(mockedAccount)).thenReturn(
                Map.of(
                        userUpgrade.getName(), userUpgrade,
                        userUpgradeWithCondition.getName(), userUpgradeWithCondition,
                        youtube.getName(), UpgradeMocks.getUpgradeMock(youtube.getName(), youtube.getLevel())
                )
        );

        mockGetById(mockedAccount.getId(), mockedAccount);
        mockAccountSave(mockedAccount);

        AccountResponse response = accountService.getAccount(1L);

        verify(accountMapper, times(1)).toResponse(any(Account.class));
        verify(accountMapper, times(1)).toDto(mockedAccount);

        assertEquals(1, response.getSectionUpgrades().size());
        assertEquals(3, response.getSectionUpgrades().getFirst().getUpgrades().size());

        assertNotEquals(userUpgrades.size(), response.getSectionUpgrades().getFirst().getUpgrades().size());


        Map<String, Boolean> responseUpgrades = new HashMap<>();
        response.getSectionUpgrades().getFirst().getUpgrades()
                .forEach(u -> responseUpgrades.put(u.getName(), u.getAvailable()));

        assertTrue(responseUpgrades.containsKey("X"));
        assertTrue(responseUpgrades.containsKey("Facebook"));
        assertTrue(responseUpgrades.containsKey("YouTube"));

        assertTrue(responseUpgrades.get("X"), "X should be available, but it is not");
        assertTrue(responseUpgrades.get("YouTube"), "YouTube should be available, but it is not");
        assertFalse(responseUpgrades.get("Facebook"), "Facebook shouldn't be available, but it is");
    }

    @Test
    @DisplayName("Get account by id")
    void getAccountById() {
        Account account = AccountMocks.getAccountMock(1L);
        account.setId(1L);

        mockGetById(account.getId(), account);
        Account foundAccount = accountService.getById(account.getId());
        assertEquals(account.getId(), foundAccount.getId());

        assertThrows(AppException.class, () -> accountService.getById(account.getId() + 1));
    }

    @Test
    @DisplayName("Add coins")
    void addCoins() {
        Account account = AccountMocks.getAccountMock(1L);

        BigDecimal balance = new BigDecimal(0);
        BigDecimal netWorth = new BigDecimal(0);

        Level level = LevelMocks.getLevelMock(1);
        long toAdd = 30;
        when(levelService.getLevelAccordingNetWorth(any(Long.class))).thenReturn(level);

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

        Level level = LevelMocks.getLevelMock(1);
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

        Level level = LevelMocks.getLevelMock(1);
        long toAdd = 100;
        when(levelService.getLevelAccordingNetWorth(toAdd)).thenReturn(LevelMocks.getLevelMock(2));

        account.setBalanceCoins(balance);
        account.setNetWorth(netWorth);
        account.setLevel(level);

        accountService.addCoins(account, toAdd);

        assertEquals(2, account.getLevel().getValue());
    }

    @Test
    @DisplayName("When an account is retrieved, balance values must be updated depending on passiveEarn")
    void updateAccountBalance() {
        Account account = AccountMocks.getAccountMock(0L);
        account.setNetWorth(new BigDecimal(0));
        account.setPassiveEarnPerHour(36000);
        account.setLevel(LevelMocks.getLevelMock(1));

        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        Timestamp lastSync = getTimestamp(0L);
        account.setLastSyncDate(lastSync);

        try (MockedStatic<TimeUtil> ignored = mockStatic(TimeUtil.class)) {
            skipTime(lastSync, 10);

            when(levelService.getLevelAccordingNetWorth(100L)).thenReturn(LevelMocks.getLevelMock(2));
            mockAccountSave(account);

            accountService.getAccount(account.getId());

            assertEquals(new BigDecimal("100.000"), account.getBalanceCoins());
            assertEquals(new BigDecimal("100.000"), account.getNetWorth());
            assertEquals(2, account.getLevel().getValue());
            assertEquals(account.getLastSyncDate(), Timestamp.from(Instant.ofEpochMilli(lastSync.getNanos() + 10 * 1000)));
        }
    }

    @Test
    @DisplayName("When an account is retrieved, taps must be updated")
    void updateAccountTaps() {
        Account account = AccountMocks.getAccountMock(0L);
        account.setAvailableTaps(0);
        account.getLevel().setTapsRecoverPerSec(1);

        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        Timestamp lastSync = getTimestamp(0L);
        account.setLastSyncDate(lastSync);

        try (MockedStatic<TimeUtil> ignored = mockStatic(TimeUtil.class)) {
            skipTime(lastSync, 10);

            when(levelService.getLevelAccordingNetWorth(any(Long.class))).thenReturn(LevelMocks.getLevelMock(1));
            mockAccountSave(account);

            accountService.getAccount(account.getId());

            assertEquals(10, account.getAvailableTaps());
            assertEquals(account.getLastSyncDate(), Timestamp.from(Instant.ofEpochMilli(lastSync.getNanos() + 10 * 1000)));
        }
    }

    @Test
    @DisplayName("When an account is retrieved, updated taps value cannot be greater than max taps")
    void updateAccountTapsCannotBeGreaterThanMaxTaps() {
        Account account = AccountMocks.getAccountMock(0L);

        account.setAvailableTaps(0);
        account.getLevel().setMaxTaps(5);
        account.getLevel().setTapsRecoverPerSec(1);

        Timestamp lastSync = getTimestamp(0L);
        account.setLastSyncDate(lastSync);

        try (MockedStatic<TimeUtil> ignored = mockStatic(TimeUtil.class)) {
            skipTime(lastSync, 10);

            when(levelService.getLevelAccordingNetWorth(any(Long.class))).thenReturn(LevelMocks.getLevelMock(1));

            mockAccountSave(account);
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

        mockGetById(account.getId(), account);

        assertThrows(AppException.class, () -> accountService.getById(1L));
    }

    @Test
    @DisplayName("Buy first upgrade")
    void buyFirstUpgrade() {
        Account account = AccountMocks.getAccountMock(1L);
        mockGetById(account.getId(), account);
        mockAccountSave(account);

        Upgrade toBuy = UpgradeMocks.getUpgradeMock("X", 0);

        account.setBalanceCoins(new BigDecimal(1000));

        Upgrade expectedNew = UpgradeMocks.getUpgradeMock("X", 1, 100);

        when(upgradeService.findUpgradeInAccount(account, toBuy.getName())).thenReturn(Optional.empty());
        when(upgradeService.findUpgrade(toBuy.getName(), toBuy.getLevel())).thenReturn(toBuy);
        when(upgradeService.findUpgrade(toBuy.getName(), toBuy.getLevel() + 1)).thenReturn(expectedNew);
        when(accountRepository.addUpgrade(account.getId(), toBuy.getName(), expectedNew.getLevel())).thenReturn(1);

        BuyUpgradeRequest upgradeUpdateRequest = new BuyUpgradeRequest(toBuy.getName());
        accountService.buyUpgrade(upgradeUpdateRequest, account.getId());

        List<Upgrade> accountUpgrades = account.getUpgrades();
        assertEquals(1, accountUpgrades.size());
        assertEquals(expectedNew.getProfitPerHour(), account.getPassiveEarnPerHour());
        assertEquals(new BigDecimal(0), account.getBalanceCoins());
        assertEquals(expectedNew.getName(), accountUpgrades.getFirst().getName());
        assertEquals(expectedNew.getLevel(), accountUpgrades.getFirst().getLevel());
    }

    @Test
    @DisplayName("Level up upgrade")
    void levelUpUpgrade() {
        Account account = AccountMocks.getAccountMock(1L);

        List<Upgrade> existingUpgrades = new ArrayList<>();
        Upgrade x = UpgradeMocks.getUpgradeMock("X", 1);
        Upgrade snapchat = UpgradeMocks.getUpgradeMock("Snapchat", 1);
        Upgrade youtube = UpgradeMocks.getUpgradeMock("YouTube", 1);
        existingUpgrades.add(x);
        existingUpgrades.add(snapchat);
        existingUpgrades.add(youtube);
        account.setUpgrades(existingUpgrades);

        mockGetById(account.getId(), account);
        mockAccountSave(account);

        int initialPassiveEarn = existingUpgrades.stream().mapToInt(Upgrade::getProfitPerHour).sum();
        account.setPassiveEarnPerHour(initialPassiveEarn);

        account.setBalanceCoins(new BigDecimal(1000));

        Upgrade newUpgrade = UpgradeMocks.getUpgradeMock(x.getName(), 2, 200);

        when(upgradeService.findUpgradeInAccount(account, x.getName())).thenReturn(Optional.of(x));
        when(upgradeService.findUpgrade(x.getName(), x.getLevel() + 1)).thenReturn(newUpgrade);
        when(accountRepository.updateUpgradeLevel(account.getId(), x.getName(), newUpgrade.getLevel())).thenReturn(1);

        BuyUpgradeRequest upgradeUpdateRequest = new BuyUpgradeRequest(x.getName());
        accountService.buyUpgrade(upgradeUpdateRequest, account.getId());

        List<Upgrade> accountUpgrades = account.getUpgrades();
        assertEquals(3, accountUpgrades.size());
        assertEquals(initialPassiveEarn - x.getProfitPerHour() + newUpgrade.getProfitPerHour(), account.getPassiveEarnPerHour());
        assertEquals(new BigDecimal(0), account.getBalanceCoins());

        int lvl1Upgrades = 0;
        int lvl2Upgrades = 0;
        for (Upgrade upgrade : accountUpgrades) {
            if (upgrade.getLevel() == 1) {
                lvl1Upgrades++;
            } else if (upgrade.getLevel() == 2) {
                lvl2Upgrades++;
            }
        }

        assertEquals(2, lvl1Upgrades);
        assertEquals(1, lvl2Upgrades);

        assertTrue(accountUpgrades.stream().anyMatch(u -> u.getName().equals(x.getName()) && u.getLevel().equals(2)));
    }

    @Test
    @DisplayName("Upgrade is already max level")
    void upgradeNotInAccount() {
        Account account = AccountMocks.getAccountMock(1L);
        mockGetById(account.getId(), account);

        Upgrade toBuy = UpgradeMocks.getUpgradeMock("1", 10);
        toBuy.setMaxLevel(true);
        List<Upgrade> upgrades = new ArrayList<>();
        upgrades.add(toBuy);
        upgrades.add(UpgradeMocks.getUpgradeMock("2", 0));
        account.setUpgrades(upgrades);

        when(upgradeService.findUpgradeInAccount(account, toBuy.getName())).thenReturn(Optional.of(toBuy));
        BuyUpgradeRequest upgradeUpdateRequest = new BuyUpgradeRequest(toBuy.getName());

        assertThrows(AppException.class, () -> accountService.buyUpgrade(upgradeUpdateRequest, account.getId()));
    }

    @Test
    @DisplayName("Buy upgrade not enough money")
    void buyUpgradeNotEnoughMoney() {
        Account account = AccountMocks.getAccountMock(1L);
        mockGetById(account.getId(), account);

        Upgrade toUpdate = UpgradeMocks.getUpgradeMock("1", 0);
        toUpdate.setPrice(10000);
        List<Upgrade> upgrades = new ArrayList<>();
        upgrades.add(toUpdate);
        upgrades.add(UpgradeMocks.getUpgradeMock("2", 0));
        account.setUpgrades(upgrades);

        account.setBalanceCoins(new BigDecimal(toUpdate.getPrice() - 1));

        when(upgradeService.findUpgradeInAccount(account, toUpdate.getName())).thenReturn(Optional.of(toUpdate));

        BuyUpgradeRequest upgradeUpdateRequest = new BuyUpgradeRequest(toUpdate.getName());
        assertThrows(AppException.class, () -> accountService.buyUpgrade(upgradeUpdateRequest, account.getId()));
    }

    @Test
    @DisplayName("Update upgrade condition not completed")
    void updateUpgradeConditionNotCompleted() {
        Account account = AccountMocks.getAccountMock(1L);
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        List<Upgrade> upgrades = new ArrayList<>();
        upgrades.add(UpgradeMocks.getUpgradeMock("1", 0));
        Upgrade toUpdate = UpgradeMocks.getUpgradeMock("2", 0);
        toUpdate.setCondition(ConditionMocks.getConditionMock("1", 1));
        upgrades.add(toUpdate);
        account.setUpgrades(upgrades);

        account.setBalanceCoins(new BigDecimal(1000));

        when(upgradeService.findUpgradeInAccount(account, toUpdate.getName())).thenReturn(Optional.of(toUpdate));

        BuyUpgradeRequest upgradeUpdateRequest = new BuyUpgradeRequest(toUpdate.getName());
        assertThrows(AppException.class, () -> accountService.buyUpgrade(upgradeUpdateRequest, account.getId()));
    }

    private void skipTime(Timestamp start, long seconds) {
        Timestamp now = Timestamp.from(Instant.ofEpochMilli(start.getTime() + seconds * 1000));

        when(TimeUtil.getCurrentTimestamp()).thenReturn(now);
        when(TimeUtil.getDifferenceInSeconds(start, now)).thenReturn(seconds);
    }

    private Timestamp getTimestamp(Long value) {
        return Timestamp.from(Instant.ofEpochMilli(value));
    }

    private void mockGetById(Long id, Account account) {
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));
    }

    private void mockAccountSave(Account account) {
        when(accountRepository.save(account)).thenReturn(account);
    }
}
