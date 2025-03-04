package com.vcasino.clicker.repository;

import com.vcasino.clicker.entity.AccountTaskRewardReceived;
import com.vcasino.clicker.entity.key.AccountTaskKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface AccountTaskRewardsReceivedRepository extends JpaRepository<AccountTaskRewardReceived, AccountTaskKey> {
    @Query("SELECT atrr.taskId FROM AccountTaskRewardReceived atrr WHERE atrr.accountId = :accountId")
    Set<Integer> findTaskIdsByAccountId(@Param(value = "accountId") Long accountId);
}
