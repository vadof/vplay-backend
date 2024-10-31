package com.vcasino.clicker.repository;

import com.vcasino.clicker.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Integer> {

    @Query("FROM Reward r WHERE r.validFrom <= :now AND (r.endsIn IS NULL OR r.endsIn >= :now)")
    List<Reward> findAllInInterval(@Param("now") LocalDateTime now);
}
