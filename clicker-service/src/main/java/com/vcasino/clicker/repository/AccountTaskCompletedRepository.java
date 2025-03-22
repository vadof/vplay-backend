package com.vcasino.clicker.repository;

import com.vcasino.clicker.entity.AccountCompletedTasks;
import com.vcasino.clicker.entity.key.AccountTaskKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface AccountTaskCompletedRepository extends JpaRepository<AccountCompletedTasks, AccountTaskKey> {
    @Query("SELECT act.taskId FROM AccountCompletedTasks act WHERE act.accountId = :accountId")
    Set<Integer> findTaskIdsByAccountId(@Param(value = "accountId") Long accountId);
}
