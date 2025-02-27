package com.vcasino.clicker.mock;

import com.vcasino.clicker.dto.UpgradeDto;
import com.vcasino.clicker.entity.Upgrade;

public class UpgradeMocks {

    public static Upgrade getUpgradeMock(String name, Integer level) {
        return Upgrade.builder()
                .name(name)
                .section(SectionMocks.getSectionMock())
                .level(level)
                .profitPerHour(0)
                .profitPerHourDelta(200)
                .price(1000)
                .condition(null)
                .maxLevel(false)
                .build();
    }

    public static Upgrade getUpgradeMock(String name, Integer level, Integer profitPerHour) {
        return Upgrade.builder()
                .name(name)
                .section(SectionMocks.getSectionMock())
                .level(level)
                .profitPerHour(profitPerHour)
                .profitPerHourDelta(200)
                .price(1000)
                .condition(null)
                .maxLevel(false)
                .build();
    }

    public static UpgradeDto getUpgradeDtoMock(String name, Integer level) {
        return UpgradeDto.builder()
                .name(name)
                .section("Social")
                .level(level)
                .profitPerHour(0)
                .profitPerHourDelta(200)
                .price(1000)
                .condition(null)
                .maxLevel(false)
                .available(true)
                .build();
    }
}
