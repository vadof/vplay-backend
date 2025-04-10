package com.vcasino.bet.entity.market.winner;

import com.vcasino.bet.entity.Match;
import com.vcasino.bet.entity.market.Market;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "market")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WinnerMap extends Market {

    @Column(name = "map_number")
    Integer mapNumber;

    public WinnerMap(Match match, Integer mapNumber, BigDecimal outcome, BigDecimal odds) {
        super(match, outcome, odds);
        this.mapNumber = mapNumber;
    }
}