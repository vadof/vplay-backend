package com.vcasino.odds.entity.market.handicap;

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
public class HandicapMaps extends Market {
    @Column(name = "participant")
    Integer participant;

    public HandicapMaps(Match match, Integer participant, BigDecimal outcome, BigDecimal odds) {
        super(match, outcome, odds);
        this.participant = participant;
    }
}
