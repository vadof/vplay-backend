package com.vcasino.clicker.repository;

import com.vcasino.clicker.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
            UPDATE account_upgrade
            SET upgrade_level = :upgradeLevel
            WHERE account_id = :accountId AND upgrade_name = :upgradeName""")
    int updateUpgradeLevel(@Param("accountId") Long accountId,
                           @Param("upgradeName") String upgradeName,
                           @Param("upgradeLevel") Integer upgradeLevel);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
        INSERT INTO account_upgrade (account_id, upgrade_name, upgrade_level)
        VALUES (:accountId, :upgradeName, :upgradeLevel)
        ON CONFLICT (account_id, upgrade_name) DO NOTHING""")
    int addUpgrade(@Param("accountId") Long accountId,
                   @Param("upgradeName") String upgradeName,
                   @Param("upgradeLevel") Integer upgradeLevel);

}
