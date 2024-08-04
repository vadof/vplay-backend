package com.vcasino.clicker.repository;

import com.vcasino.clicker.entity.Upgrade;
import com.vcasino.clicker.entity.id.key.UpgradeKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UpgradeRepository extends JpaRepository<Upgrade, UpgradeKey> {
    List<Upgrade> findAllByLevel(Integer level);
}
