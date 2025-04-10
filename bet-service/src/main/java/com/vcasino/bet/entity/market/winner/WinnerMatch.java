package com.vcasino.bet.entity.market.winner;

import com.vcasino.bet.entity.Match;
import com.vcasino.bet.entity.market.Market;
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