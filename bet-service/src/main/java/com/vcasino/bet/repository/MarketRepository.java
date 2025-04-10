package com.vcasino.bet.repository;

import com.vcasino.bet.entity.market.Market;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketRepository extends JpaRepository<Market, Long> {
}
