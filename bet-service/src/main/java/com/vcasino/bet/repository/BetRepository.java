package com.vcasino.bet.repository;

import com.vcasino.bet.entity.Bet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BetRepository extends JpaRepository<Bet, Long> {
    List<Bet> findAllByMarketId(Long marketId);

    Page<Bet> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query(value = """
        SELECT DISTINCT m.market_id
        FROM bet b
            JOIN market m ON b.market_id = m.market_id
        WHERE b.result IS NULL AND m.result IS NOT NULL;
    """, nativeQuery = true)
    List<Long> findDistinctMarketIdsWithUnsettledBetsAndResolvedMarkets();
}
