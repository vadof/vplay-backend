package com.vcasino.clicker.service;

import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.entity.Upgrade;
import com.vcasino.clicker.entity.key.UpgradeKey;
import com.vcasino.clicker.exception.AppException;
import com.vcasino.clicker.mock.AccountMocks;
import com.vcasino.clicker.mock.UpgradeMocks;
import com.vcasino.clicker.repository.UpgradeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * UNIT tests for {@link UpgradeService}
 */
@ExtendWith(MockitoExtension.class)
public class UpgradeServiceTest {

    @Mock
    private UpgradeRepository upgradeRepository;

    @InjectMocks
    private UpgradeService upgradeService;

    @Test
    @DisplayName("Calculate passive earn per hour")
    void calculatePassiveEarnPerHour() {
        List<Upgrade> upgrades = new ArrayList<>();

        int expectedResult = 0;
        for (int i = 0; i < 10; i++) {
            int profit = i * 100;
            expectedResult += profit;
            upgrades.add(UpgradeMocks.getUpgradeMock(i + "", i, profit));
        }

        Integer result = upgradeService.calculatePassiveEarnPerHour(upgrades);

        assertEquals(expectedResult, result);
    }

    @Test
    @DisplayName("Find upgrade")
    void findUpgrade() {
        Upgrade x = UpgradeMocks.getUpgradeMock("X", 0, 0);
        when(upgradeRepository.findById(new UpgradeKey("X", 0))).thenReturn(Optional.of(x));

        Upgrade foundUpgrade = upgradeService.findUpgrade(x.getName(), x.getLevel());
        assertEquals(x.getName(), foundUpgrade.getName());
        assertEquals(x.getLevel(), foundUpgrade.getLevel());
        assertEquals(x.getSection(), foundUpgrade.getSection());
        assertEquals(x.getMaxLevel(), foundUpgrade.getMaxLevel());
        assertEquals(x.getProfitPerHour(), foundUpgrade.getProfitPerHour());
        assertEquals(x.getProfitPerHourDelta(), foundUpgrade.getProfitPerHourDelta());
        assertEquals(x.getPrice(), foundUpgrade.getPrice());
    }

    @Test
    @DisplayName("Find upgrade not found")
    void findUpgradeNotFound() {
        Upgrade x = UpgradeMocks.getUpgradeMock("X", 0, 0);
        when(upgradeRepository.findById(new UpgradeKey("X", 0))).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> upgradeService.findUpgrade(x.getName(), x.getLevel()));
    }

    @Test
    @DisplayName("Find upgrade in account")
    void findUpgradeInAccount() {
        Account account = AccountMocks.getAccountMock(1L);
        Upgrade toFind = UpgradeMocks.getUpgradeMock("1", 0);

        account.getUpgrades().add(toFind);
        account.getUpgrades().add(UpgradeMocks.getUpgradeMock("2", 1));
        account.getUpgrades().add(UpgradeMocks.getUpgradeMock("3", 2));
        account.getUpgrades().add(UpgradeMocks.getUpgradeMock("4", 3));

        Optional<Upgrade> optionalUpgrade = upgradeService.findUpgradeInAccount(account, toFind.getName());

        assertTrue(optionalUpgrade.isPresent());

        Upgrade upgrade = optionalUpgrade.get();

        assertEquals(toFind.getName(), upgrade.getName());
        assertEquals(toFind.getLevel(), upgrade.getLevel());
        assertEquals(toFind.getPrice(), upgrade.getPrice());
        assertEquals(toFind.getProfitPerHour(), upgrade.getProfitPerHour());
        assertEquals(toFind.getMaxLevel(), upgrade.getMaxLevel());
        assertEquals(toFind.getSection().getName(), upgrade.getSection().getName());

        Upgrade notInAcc = UpgradeMocks.getUpgradeMock("5", 0);
        assertTrue(upgradeService.findUpgradeInAccount(account, notInAcc.getName()).isEmpty());
    }

    @Test
    @DisplayName("Get all upgrades including missing")
    void getAllUpgradesIncludingMissing() throws NoSuchFieldException, IllegalAccessException {
        Account account = AccountMocks.getAccountMock(1L);

        account.getUpgrades().add(UpgradeMocks.getUpgradeMock("1", 1));
        account.getUpgrades().add(UpgradeMocks.getUpgradeMock("2", 1));

        List<Upgrade> initialUpgrades = new ArrayList<>(2);
        initialUpgrades.add(UpgradeMocks.getUpgradeMock("3", 0));
        initialUpgrades.add(UpgradeMocks.getUpgradeMock("4", 0));

        Field field = UpgradeService.class.getDeclaredField("initialUpgrades");
        field.setAccessible(true);
        field.set(upgradeService, initialUpgrades);

        Map<String, Upgrade> res = upgradeService.getAllUpgradesIncludingMissing(account);

        for (int i = 1; i <= 4; i++) {
            assertTrue(res.containsKey(String.valueOf(i)));
        }
    }

}
