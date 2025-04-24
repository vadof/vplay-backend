package com.vcasino.bet.dto.statistics;

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
public class AdditionalMatchStatistics {
    Long betCount;
    BigDecimal totalAmountWagered;
    BigDecimal totalAmountWin;
    BigDecimal totalAmountLoss;
    BigDecimal profit;
    Long totalWinBets;
    Long totalCancelledBets;
    Long totalLossBets;
}
