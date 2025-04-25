package com.vcasino.odds.repository;

import com.vcasino.odds.entity.Match;
import com.vcasino.odds.entity.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByStartDateBeforeAndStatusNot(LocalDateTime threshold, MatchStatus status);
}
