package com.vcasino.bet.dto.statistics.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInformationDto {
    Long userId;
    Integer totalBetsPlaced;
    Integer totalWinBets;
    Integer totalCancelledBets;
    Integer totalLossBets;
    BigDecimal totalAmountWagered;
    BigDecimal totalWinAmount;
    BigDecimal totalLossAmount;
    BigDecimal biggestBet;
    BigDecimal smallestBet;
    BigDecimal averageBet;
    Integer totalTournamentsParticipated;
    Integer totalMatchesParticipated;
    BigDecimal winPercentage;
    List<UserBetDto> latestBets;
}
