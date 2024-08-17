package com.vcasino.clicker.mapper;

import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.dto.ConditionDto;
import com.vcasino.clicker.dto.SectionUpgradesDto;
import com.vcasino.clicker.dto.UpgradeDto;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.entity.Upgrade;
import com.vcasino.clicker.mapper.common.EntityMapper;
import com.vcasino.clicker.service.UpgradeService;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
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

    @Autowired
    private UpgradeService upgradeService;

    @Autowired
    private UpgradeMapper upgradeMapper;

    @Override
    public abstract AccountDto toDto(Account entity);

    @AfterMapping
    protected void setSectionUpgrades(@MappingTarget AccountDto dto, Account entity) {
        List<SectionUpgradesDto> sectionUpgradesList = new ArrayList<>(upgradeService.getSectionUpgradesList());

        Map<String, Upgrade> userUpgrades = new HashMap<>();
        entity.getUpgrades().forEach(u -> userUpgrades.put(u.getName(), u));

        for (SectionUpgradesDto sectionUpgrades : sectionUpgradesList) {
            List<UpgradeDto> updatedUpgrades = new ArrayList<>(10);

            for (UpgradeDto upgrade : sectionUpgrades.getUpgrades()) {
                UpgradeDto userUpgrade =  upgradeMapper.toDto(userUpgrades.get(upgrade.getName()));
                setAvailable(userUpgrade, userUpgrades);
                updatedUpgrades.add(userUpgrade);
            }

            sectionUpgrades.setUpgrades(updatedUpgrades);
        }

        dto.setSectionUpgrades(sectionUpgradesList);
    }

    protected void setAvailable(UpgradeDto userUpgrade, Map<String, Upgrade> userUpgrades) {
        ConditionDto condition = userUpgrade.getCondition();
        if (condition == null) {
            userUpgrade.setAvailable(true);
        } else {
            switch (condition.getType()) {
                case BY_UPGRADE -> userUpgrade.setAvailable(isAvailableByUpgrade(condition, userUpgrades));
                default -> userUpgrade.setAvailable(false);
            }
        }
    }

    @AfterMapping
    protected void calculatePassiveEarnPerSec(@MappingTarget AccountDto dto) {
        dto.setPassiveEarnPerSec(dto.getPassiveEarnPerHour() / 3600d);
    }

    protected boolean isAvailableByUpgrade(ConditionDto condition, Map<String, Upgrade> currentUpgradeLevels) {
        return currentUpgradeLevels.get(condition.getUpgradeName()).getLevel() >= condition.getLevel();
    }

}
