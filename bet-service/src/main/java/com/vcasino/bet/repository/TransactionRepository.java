package com.vcasino.bet.repository;


import com.vcasino.bet.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    @Query("SELECT t.eventId FROM Transaction t WHERE t.eventId IN :eventIds")
    List<UUID> findExistingEventIds(@Param("eventIds") List<UUID> eventIds);
}
