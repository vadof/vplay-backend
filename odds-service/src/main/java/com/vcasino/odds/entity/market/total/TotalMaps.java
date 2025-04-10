package com.vcasino.odds.entity.market.total;

import com.vcasino.odds.entity.Match;
import com.vcasino.odds.entity.market.Market;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@Table(name = "market")
public class TotalMaps extends Market {
    public TotalMaps(Match match, BigDecimal outcome, BigDecimal odds) {
        super(match, outcome, odds);
    }
}
