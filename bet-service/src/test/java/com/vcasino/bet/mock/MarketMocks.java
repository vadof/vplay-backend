package com.vcasino.bet.mock;

import com.vcasino.bet.entity.Match;
import com.vcasino.bet.entity.market.Market;
import com.vcasino.bet.entity.market.handicap.HandicapMaps;
import com.vcasino.bet.entity.market.total.TotalMapRounds;
import com.vcasino.bet.entity.market.total.TotalMaps;
import com.vcasino.bet.entity.market.winner.WinnerMap;
import com.vcasino.bet.entity.market.winner.WinnerMatch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MarketMocks {

    public static List<Market> getMarketPairMocks(Match match, String type, int totalMaps) {
        List<Market> markets = new ArrayList<>();
        if (type.equals("WinnerMatch")) {
            markets.addAll(List.of(
                    new WinnerMatch(match, BigDecimal.ONE, BigDecimal.valueOf(1.9)),
                    new WinnerMatch(match, BigDecimal.TWO, BigDecimal.valueOf(1.9))
            ));
        } else if (type.equals("WinnerMap")) {
            for (int i = 1; i < totalMaps; i++) {
                markets.addAll(List.of(
                        new WinnerMap(match, i, BigDecimal.valueOf(-19.5), BigDecimal.valueOf(1.9)),
                        new WinnerMap(match, i, BigDecimal.valueOf(+19.5), BigDecimal.valueOf(1.9))
                ));
            }
        } else if (type.equals("TotalMaps")) {
            markets.addAll(List.of(
                    new TotalMaps(match, BigDecimal.valueOf(-2.5), BigDecimal.valueOf(1.9)),
                    new TotalMaps(match, BigDecimal.valueOf(2.5), BigDecimal.valueOf(1.9))
            ));
        } else if (type.equals("TotalMapRounds")) {
            for (int i = 1; i <= totalMaps; i++) {
                markets.addAll(List.of(
                        new TotalMapRounds(match, i, BigDecimal.valueOf(-19.5), BigDecimal.valueOf(1.9)),
                        new TotalMapRounds(match, i, BigDecimal.valueOf(+19.5), BigDecimal.valueOf(1.9))
                ));
            }
        } else if (type.equals("HandicapMaps")) {
            markets.addAll(List.of(
                    new HandicapMaps(match, 1, BigDecimal.valueOf(-1.5), BigDecimal.valueOf(1.9)),
                    new HandicapMaps(match, 2, BigDecimal.valueOf(1.5), BigDecimal.valueOf(1.9)),
                    new HandicapMaps(match, 2, BigDecimal.valueOf(-1.5), BigDecimal.valueOf(1.9)),
                    new HandicapMaps(match, 1, BigDecimal.valueOf(1.5), BigDecimal.valueOf(1.9))
            ));
        } else {
            throw new RuntimeException("Unknown Market");
        }

        for (int i = 0; i < markets.size(); i++) {
            markets.get(i).setId(i + 1L);
            markets.get(i).setType(type);
        }

        return markets;
    }

}
