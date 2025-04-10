package com.vcasino.odds.repository;

import com.vcasino.odds.entity.market.Market;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketRepository extends JpaRepository<Market, Long> {
}
