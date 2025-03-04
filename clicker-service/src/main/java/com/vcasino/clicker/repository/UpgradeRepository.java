package com.vcasino.clicker.repository;

import com.vcasino.clicker.entity.Upgrade;
import com.vcasino.clicker.entity.key.UpgradeKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UpgradeRepository extends JpaRepository<Upgrade, UpgradeKey> {
    List<Upgrade> findAllByLevel(Integer level);

    @Query(value = """
            SELECT section
            FROM (SELECT section, MAX(price) AS max_price
                  FROM upgrade
                  GROUP BY section
                  ORDER BY max_price) as smp
            """, nativeQuery = true)
    List<String> getSectionNamesSortedByMostExpensiveUpgrade();
}
