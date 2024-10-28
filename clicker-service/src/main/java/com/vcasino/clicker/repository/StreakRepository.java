package com.vcasino.clicker.repository;

import com.vcasino.clicker.entity.Streak;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreakRepository extends JpaRepository<Streak, Long> {
}
