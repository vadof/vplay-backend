package com.vcasino.bet.dto.ws;

import com.vcasino.bet.dto.response.MatchMapDto;
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
public class MatchUpdateDto {
    Long matchId;
    List<MarketWsDto> winnerMatchMarkets;
    List<MatchMapDto> matchMaps;
    Boolean ended;
}
