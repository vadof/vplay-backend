package com.vcasino.bet.repository;

import com.vcasino.bet.entity.market.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MarketRepository extends JpaRepository<Market, Long> {
    @Query("SELECT m FROM Market m WHERE m.id IN :marketIds")
    List<Market> findMarketsByIds(@Param("marketIds") List<Long> marketIds);

}
