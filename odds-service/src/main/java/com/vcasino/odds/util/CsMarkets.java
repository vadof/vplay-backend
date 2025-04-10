package com.vcasino.odds.util;

import com.vcasino.odds.entity.market.Market;
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
public class CsMarkets {
    List<Market> winnerMatch;
    List<Market> winnerMap;

    List<Market> totalMaps;
    List<Market> totalMapRounds;

    List<Market> handicapMaps;
}
