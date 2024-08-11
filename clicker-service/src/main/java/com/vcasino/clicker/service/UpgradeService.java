package com.vcasino.clicker.service;

import com.vcasino.clicker.dto.SectionUpgradesDto;
import com.vcasino.clicker.dto.UpgradeDto;
import com.vcasino.clicker.entity.Upgrade;
import com.vcasino.clicker.mapper.UpgradeMapper;
import com.vcasino.clicker.repository.UpgradeRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class UpgradeService {

    private final UpgradeRepository upgradeRepository;
    private final UpgradeMapper upgradeMapper;

    @Getter
    private List<Upgrade> initialUpgrades;

    @Getter
    private List<SectionUpgradesDto> sectionUpgradesList;

    public Integer calculatePassiveEarnPerHour(List<Upgrade> upgrades) {
        return upgrades.stream()
                .mapToInt(Upgrade::getProfitPerHour)
                .sum();
    }

    @PostConstruct
    private void cacheUpgrades() {
        initialUpgrades = upgradeRepository.findAllByLevel(0);

        Map<String, List<UpgradeDto>> upgradesBySection = new HashMap<>();

        List<String> sectionNames = upgradeRepository.getSectionNamesSortedByMostExpensiveUpgrade();
        sectionNames.forEach(s -> upgradesBySection.put(s, new ArrayList<>()));

        List<UpgradeDto> upgradeList = upgradeMapper.toDtos(initialUpgrades);
        upgradeList.forEach(u -> upgradesBySection.get(u.getSection()).add(u));

        for (int i = 0; i < sectionNames.size(); i++) {
            String section = sectionNames.get(i);
            List<UpgradeDto> upgrades = upgradesBySection.get(section);
            upgrades.sort(Comparator.comparingInt(UpgradeDto::getPrice));

            sectionUpgradesList.add(SectionUpgradesDto.builder()
                    .order(i)
                    .section(section)
                    .upgrades(upgrades)
                    .build()
            );
        }
    }

}
