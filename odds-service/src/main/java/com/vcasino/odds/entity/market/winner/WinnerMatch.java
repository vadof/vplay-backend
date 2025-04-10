package com.vcasino.odds.entity.market.winner;

import com.vcasino.odds.entity.Match;
import com.vcasino.odds.entity.market.Market;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@Table(name = "market")
public class WinnerMatch extends Market {
    public WinnerMatch(Match match, BigDecimal outcome, BigDecimal odds) {
        super(match, outcome, odds);
    }
}