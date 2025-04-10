package com.vcasino.odds.repository;

import com.vcasino.odds.entity.Match;
import com.vcasino.odds.entity.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    @Query("FROM Match WHERE tournament.id = :id")
    List<Match> findAllByTournamentId(@Param("id") Integer id);

    List<Match> findByStartDateBeforeAndStatus(LocalDateTime threshold, MatchStatus status);
}
