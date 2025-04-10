package com.vcasino.bet.repository;

import com.vcasino.bet.entity.Match;
import com.vcasino.bet.entity.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    @Query("FROM Match WHERE tournament.id = :id")
    List<Match> findAllByTournamentId(@Param("id") Integer id);

    List<Match> findByStartDateBeforeAndStatus(LocalDateTime threshold, MatchStatus status);

    boolean existsByMatchPage(String page);
}
