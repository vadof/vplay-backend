package com.vcasino.bet.repository;

import com.vcasino.bet.entity.Bet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BetRepository extends JpaRepository<Bet, Long> {
    List<Bet> findAllByMarketId(Long marketId);
    Page<Bet> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
