package com.vcasino.bet.dto.statistics.market;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MarketStatistics {
    Long matchId;
    Long marketId;
    String marketType;
    BigDecimal outcome;
    Boolean closed;
    Integer participant;
    Integer mapNumber;
    String result;
    Integer betCount;
    BigDecimal totalAmountWagered;
    BigDecimal totalAmountWin;
    BigDecimal totalAmountLoss;
    BigDecimal averageBetAmount;
    BigDecimal maxBetAmount;
}
