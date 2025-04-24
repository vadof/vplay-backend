package com.vcasino.bet.dto.statistics;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatchStatisticsDto {
    Long matchId;
    String matchPage;
    String format;
    String startDate;
    String status;
    Integer winner;
    Double winProbability1;
    Double winProbability2;
    String tournamentPage;
    String tournamentTitle;
    String discipline;
    String participant1Name;
    String participant2Name;
    AdditionalMatchStatistics additionalStatistics;
}
