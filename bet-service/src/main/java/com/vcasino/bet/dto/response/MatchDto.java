package com.vcasino.bet.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatchDto {
    Long id;
    ParticipantDto participant1;
    ParticipantDto participant2;
    Long startDate;
    MarketPairDto winnerMatchMarkets;
    List<MatchMapDto> matchMaps;
}
