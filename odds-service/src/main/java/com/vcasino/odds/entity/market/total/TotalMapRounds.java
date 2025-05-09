package com.vcasino.odds.entity.market.total;

import com.vcasino.odds.entity.Match;
import com.vcasino.odds.entity.market.Market;
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
public class TotalMapRounds extends Market {
    @Column(name = "map_number")
    Integer mapNumber;

    public TotalMapRounds(Match match, Integer mapNumber, BigDecimal outcome, BigDecimal odds) {
        super(match, outcome, odds);
        this.mapNumber = mapNumber;
    }
}
