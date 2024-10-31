package com.vcasino.clicker.repository;

import com.vcasino.clicker.entity.AccountRewardsReceived;
import com.vcasino.clicker.entity.id.key.AccountRewardKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface AccountRewardsReceivedRepository extends JpaRepository<AccountRewardsReceived, AccountRewardKey> {
    @Query("SELECT arr.rewardId FROM AccountRewardsReceived arr WHERE arr.accountId = :accountId")
    Set<Integer> findRewardIdsByAccountId(@Param(value = "accountId") Long accountId);
}
