package com.vcasino.clickerdata.repository;

import com.vcasino.clickerdata.entity.TotalClicks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface TotalClicksRepository extends JpaRepository<TotalClicks, LocalDateTime> {

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO total_clicks (date, amount) VALUES (:date, :amount)
            ON CONFLICT (date) DO UPDATE SET amount = total_clicks.amount + EXCLUDED.amount
            """, nativeQuery = true)
    void insertTotalClicks(@Param("date") LocalDateTime date, @Param("amount") long amount);

}
