package com.vcasino.clicker.service;

import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.entity.Upgrade;
import com.vcasino.clicker.mapper.AccountMapper;
import com.vcasino.clicker.mapper.AccountMapperImpl;
import com.vcasino.clicker.mapper.ConditionMapper;
import com.vcasino.clicker.mapper.ConditionMapperImpl;
import com.vcasino.clicker.mapper.SectionMapper;
import com.vcasino.clicker.mapper.SectionMapperImpl;
import com.vcasino.clicker.mapper.UpgradeMapper;
import com.vcasino.clicker.mapper.UpgradeMapperImpl;
import com.vcasino.clicker.mock.AccountMocks;
import com.vcasino.clicker.mock.LevelMocks;
import com.vcasino.clicker.mock.UpgradeMocks;
import com.vcasino.clicker.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** UNIT tests for {@link AccountService} */

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
        ReflectionTestUtils.setField(upgradeMapper, "sectionMapper", sectionMapper);
        ReflectionTestUtils.setField(upgradeMapper, "conditionMapper", conditionMapper);
    }

    @Test
    @DisplayName("Create Account")
    void createAccount() {
        Upgrade mockedUpgrade = UpgradeMocks.getUpgradeMock("Facebook", 0);
        when(upgradeService.getInitialUpgrades()).thenReturn(List.of(mockedUpgrade));
        when(levelService.getLevelAccordingNetWorth(0L)).thenReturn(LevelMocks.getLevelMock());

        Account mockedAccount = AccountMocks.getAccountMock(1L);
        mockedAccount.setUpgrades(List.of(mockedUpgrade));

        when(accountRepository.saveAndFlush(any(Account.class))).thenReturn(mockedAccount);

        AccountDto response = accountService.createAccount(1L);

        verify(accountMapper, times(1)).toDto(any(Account.class));

        assertEquals(mockedAccount.getLevel(), response.getLevel());
        assertEquals(mockedAccount.getBalanceCoins(), response.getBalanceCoins());
        assertEquals(mockedAccount.getNetWorth(), response.getNetWorth());
        assertEquals(mockedAccount.getUpgrades().size(), response.getUpgrades().size());
        assertEquals(mockedUpgrade.getName() ,response.getUpgrades().get(0).getName());
        assertEquals(mockedUpgrade.getLevel() ,response.getUpgrades().get(0).getLevel());
        assertTrue(response.getUpgrades().get(0).getAvailable());
        assertEquals(mockedAccount.getEarnPassivePerHour(), response.getEarnPassivePerHour());
        assertEquals(mockedAccount.getAvailableTaps(), response.getAvailableTaps());
        assertEquals(mockedAccount.getMaxTaps(), response.getMaxTaps());
        assertEquals(mockedAccount.getEarnPerTap(), response.getEarnPerTap());
        assertEquals(mockedAccount.getTapsRecoverPerSec(), response.getTapsRecoverPerSec());
    }
}
