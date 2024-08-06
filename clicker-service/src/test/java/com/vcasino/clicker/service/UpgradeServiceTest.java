package com.vcasino.clicker.service;


import com.vcasino.clicker.entity.Upgrade;
import com.vcasino.clicker.mock.UpgradeMocks;
import com.vcasino.clicker.repository.UpgradeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

}
