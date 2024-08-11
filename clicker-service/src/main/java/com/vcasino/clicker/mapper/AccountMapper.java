package com.vcasino.clicker.mapper;

import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.dto.ConditionDto;
import com.vcasino.clicker.dto.UpgradeDto;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.mapper.common.EntityMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {UpgradeMapper.class}
)
public abstract class AccountMapper implements EntityMapper<Account, AccountDto> {

    @Override
    public abstract AccountDto toDto(Account entity);

    @AfterMapping
    protected void markAvailableUpgrades(@MappingTarget AccountDto dto) {
        List<UpgradeDto> upgrades = dto.getUpgrades();

        Map<String, Integer> currentUpgradeLevels = new HashMap<>();
        upgrades.forEach(u -> currentUpgradeLevels.put(u.getName(), u.getLevel()));

        for (UpgradeDto upgrade : upgrades) {
            ConditionDto condition = upgrade.getCondition();

            if (condition == null) {
                upgrade.setAvailable(true);
            } else {
                switch (condition.getType()) {
                    case BY_UPGRADE -> upgrade.setAvailable(isAvailableByUpgrade(condition, currentUpgradeLevels));
                    default -> upgrade.setAvailable(false);
                }
            }
        }
    }

    @AfterMapping
    protected void calculatePassiveEarnPerSec(@MappingTarget AccountDto dto) {
        dto.setPassiveEarnPerSec(dto.getPassiveEarnPerHour() / 3600d);
    }

    private boolean isAvailableByUpgrade(ConditionDto condition, Map<String, Integer> currentUpgradeLevels) {
        return currentUpgradeLevels.get(condition.getUpgradeName()) >= condition.getLevel();
    }

}
