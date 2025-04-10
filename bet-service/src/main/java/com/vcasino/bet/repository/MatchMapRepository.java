package com.vcasino.bet.repository;

import com.vcasino.bet.entity.MatchMap;
import com.vcasino.bet.entity.key.MatchMapKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchMapRepository extends JpaRepository<MatchMap, MatchMapKey> {
    @Query("FROM MatchMap WHERE match.id = :matchId")
    List<MatchMap> findAllByMatchId(@Param("matchId") Long matchId);
}
