package com.vcasino.clicker.service;

import com.vcasino.clicker.entity.Upgrade;
import com.vcasino.clicker.repository.UpgradeRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class UpgradeService {

    private final UpgradeRepository upgradeRepository;

    public List<Upgrade> getInitialUpgrades() {
        return upgradeRepository.findAllByLevel(0);
    }

}
