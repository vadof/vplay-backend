package com.vcasino.odds.service;

import com.vcasino.commonkafka.enums.MarketUpdateType;
import com.vcasino.odds.entity.Match;
import com.vcasino.odds.entity.MatchMap;
import com.vcasino.odds.entity.market.Market;
import com.vcasino.odds.entity.market.MarketResult;
import com.vcasino.odds.entity.market.handicap.HandicapMaps;
import com.vcasino.odds.entity.market.total.TotalMapRounds;
import com.vcasino.odds.entity.market.total.TotalMaps;
import com.vcasino.odds.entity.market.winner.WinnerMap;
import com.vcasino.odds.entity.market.winner.WinnerMatch;
import com.vcasino.odds.repository.MarketRepository;
import com.vcasino.odds.repository.MatchMapRepository;
import com.vcasino.odds.util.MapState;
import com.vcasino.odds.util.ParticipantMapStatistics;
import com.vcasino.odds.util.RoundState;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class CsOddsService extends OddsService {

    private final MarketRepository marketRepository;
    private final MatchMapRepository matchMapRepository;
    private final MarketUpdateService marketUpdateService;
    private final Double ROUND_WIN_PERCENT = 0.075;
    private final List<Double> totalOverOutcomes = List.of(23.5, 22.5, 21.5, 20.5, 19.5, 18.5);

    public void initMatchMarkets(Match match) {
        Map<Integer, double[]> mapWinnerProbabilities = calculateInitialMapWinnerProbabilities(match);

        List<Market> winnerMatch = initWinnerMatchMarket(match);
        List<Market> winnerMap = initWinnerMapMarket(match, mapWinnerProbabilities);

        List<Market> totalMaps = initTotalMapsMarket(match);
        List<Market> totalMapRounds = initTotalMapRoundsMarket(match);

        List<Market> handicapMaps = initHandicapMarket(match, mapWinnerProbabilities);

        marketRepository.saveAll(winnerMatch);
        marketRepository.saveAll(winnerMap);
        marketRepository.saveAll(totalMaps);
        marketRepository.saveAll(totalMapRounds);
        marketRepository.saveAll(handicapMaps);

//        return new CsMarkets(winnerMatch, winnerMap, totalMaps, totalMapRounds, handicapMaps);
    }

    /**
     * @return Map where key=mapNumber, value=[team1WinProbability, team2WinProbability]
     */
    private Map<Integer, double[]> calculateInitialMapWinnerProbabilities(Match match) {
        double winProbability1 = match.getWinProbability1();
        double winProbability2 = match.getWinProbability2();

        Map<Integer, double[]> mapWinner = new HashMap<>();
        int totalMaps = Integer.parseInt(match.getFormat().substring(2));

        for (int i = 1; i <= totalMaps; i++) {
            mapWinner.put(i, new double[]{winProbability1, winProbability2});

            double percent = 0.05;
            if (winProbability1 < winProbability2) {
                winProbability1 = Math.min(0.5, winProbability1 + percent);
                winProbability2 = Math.max(0.5, winProbability2 - percent);
            } else if (winProbability1 > winProbability2) {
                winProbability1 = Math.max(0.5, winProbability1 - percent);
                winProbability2 = Math.min(0.5, winProbability2 + percent);
            }

        }

        return mapWinner;
    }

    private List<Market> initWinnerMatchMarket(Match match) {
        List<Market> winnerMatch = new ArrayList<>();
        winnerMatch.add(new WinnerMatch(match, BigDecimal.ONE, calculateOddsFromProbability(match.getWinProbability1())));
        winnerMatch.add(new WinnerMatch(match, BigDecimal.TWO, calculateOddsFromProbability(match.getWinProbability2())));
        return winnerMatch;
    }

    private List<Market> initWinnerMapMarket(Match match, Map<Integer, double[]> mapWinnerProbabilities) {
        List<Market> winnerMaps = new ArrayList<>();
        int totalMaps = mapWinnerProbabilities.size();

        if (totalMaps < 2) return winnerMaps;

        for (int i = 1; i <= totalMaps; i++) {
            double[] winProbabilities = mapWinnerProbabilities.get(i);

            winnerMaps.add(new WinnerMap(match, i, BigDecimal.ONE, calculateOddsFromProbability(winProbabilities[0])));
            winnerMaps.add(new WinnerMap(match, i, BigDecimal.TWO, calculateOddsFromProbability(winProbabilities[1])));
        }

        return winnerMaps;
    }

    private List<Market> initTotalMapsMarket(Match match) {
        List<Market> totalMaps = new ArrayList<>();

        Double outcome = null;
        if (match.getFormat().equals("BO3")) {
            outcome = 2.5;
        } else if (match.getFormat().equals("BO5")) {
            outcome = 3.5;
        }

        if (outcome == null) return totalMaps;

        double overProbability = match.getWinProbability1() > match.getWinProbability2() ?
                match.getWinProbability2() : match.getWinProbability1();

        BigDecimal overOdds = calculateOddsFromProbability(overProbability);
        BigDecimal underOdds = calculateOddsFromProbability(1 - overProbability);
        if (isOddsPassesThreshold(overOdds) && isOddsPassesThreshold(underOdds)) {
            totalMaps.add(new TotalMaps(match, BigDecimal.valueOf(outcome), overOdds));
            totalMaps.add(new TotalMaps(match, BigDecimal.valueOf(-outcome), underOdds));
        }

        return totalMaps;
    }

    private List<Market> initTotalMapRoundsMarket(Match match) {
        List<Market> totalMapRounds = new ArrayList<>();

        List<Double> overProbabilities = new ArrayList<>(totalOverOutcomes.size());
        for (int i = 0; i < totalOverOutcomes.size(); i++) {
            overProbabilities.add(0.25 + i * 0.11);
        }

        int totalMaps = Integer.parseInt(match.getFormat().substring(2));

        List<BigDecimal> overOdds = overProbabilities.stream()
                .map(this::calculateOddsFromProbability).toList();
        List<BigDecimal> underOdds = overProbabilities.stream()
                .map(odds -> calculateOddsFromProbability(1 - odds)).toList();

        for (int map = 1; map <= totalMaps; map++) {
            for (int valueI = 0; valueI < totalOverOutcomes.size(); valueI++) {
                double value = totalOverOutcomes.get(valueI);
                totalMapRounds.add(new TotalMapRounds(match, map, BigDecimal.valueOf(value), overOdds.get(valueI)));
                totalMapRounds.add(new TotalMapRounds(match, map, BigDecimal.valueOf(-value), underOdds.get(valueI)));
            }
        }

        return totalMapRounds;
    }

    private List<Market> initHandicapMarket(Match match, Map<Integer, double[]> mapWinnerProbabilities) {
        List<Market> handicapMaps = new ArrayList<>();

        if (match.getFormat().equals("BO3")) {
            double[] map1Probs = mapWinnerProbabilities.get(1);
            double[] map2Probs = mapWinnerProbabilities.get(1);

            double p1Win2_0 = Math.min(0.9, Math.max(0.1, map1Probs[0] * map2Probs[0]));
            double p2Win2_0 = Math.min(0.9, Math.max(0.1, map1Probs[1] * map2Probs[1]));

            handicapMaps.add(new HandicapMaps(match, 1, BigDecimal.valueOf(-1.5), calculateOddsFromProbability(p1Win2_0)));
            handicapMaps.add(new HandicapMaps(match, 2, BigDecimal.valueOf(1.5), calculateOddsFromProbability(1 - p1Win2_0)));
            handicapMaps.add(new HandicapMaps(match, 2, BigDecimal.valueOf(-1.5), calculateOddsFromProbability(p2Win2_0)));
            handicapMaps.add(new HandicapMaps(match, 1, BigDecimal.valueOf(1.5), calculateOddsFromProbability(1 - p2Win2_0)));
        }

        return handicapMaps;
    }

    // --------------------------- ODDS UPDATES HERE ----------------------------
    // --------------------------- ODDS UPDATES HERE ----------------------------
    // --------------------------- ODDS UPDATES HERE ----------------------------

    public void updateOddsAfterMapsInitialized(Match match, Map<Integer, ParticipantMapStatistics> mapsStatistics) {
        setMapWinnerProbabilitiesBasedOnMapStatistics(match, mapsStatistics);

        updateWinnerMatchOdds(match);
        updateWinnerMapOdds(match);
        updateTotalMapsOdds(match);
        updateHandicapOdds(match);

        for (MatchMap matchMap : match.getMatchMaps()) {
            updateTotalMapRoundsOdds(match, matchMap);
        }
    }

    public void updateOddsBasedOnRound(Match match, MapState mapState, MatchMap currentMap) {
        if (getMinRoundsBeforeMapWin(currentMap.getParticipant1Score(), currentMap.getParticipant2Score()) <= 2) {
            closeMarketsOnCurrentMap(match, currentMap);
        } else {
            double[] roundWinnerProbabilities = calculateRoundWinnerProbabilities(mapState.getRoundState(), mapState.getIsTeam1CT());
            if (roundWinnerProbabilities[0] != roundWinnerProbabilities[1]) {
                updateMapWinnerProbabilitiesBasedOnScore(currentMap);

                if (roundWinnerProbabilities[0] > roundWinnerProbabilities[1]) {
                    double wp1 = Math.min(1.0, currentMap.getCurrentWP1() + ROUND_WIN_PERCENT * roundWinnerProbabilities[0]);
                    currentMap.setCurrentWP1(wp1);
                    currentMap.setCurrentWP2(1.0 - wp1);
                } else {
                    double wp2 = Math.min(1.0, currentMap.getCurrentWP2() + ROUND_WIN_PERCENT * roundWinnerProbabilities[1]);
                    currentMap.setCurrentWP2(wp2);
                    currentMap.setCurrentWP1(1.0 - wp2);
                }

                matchMapRepository.save(currentMap);

                updateWinnerMatchOdds(match);
                updateWinnerMapOdds(match);
                updateTotalMapsOdds(match);
                updateHandicapOdds(match);
            }
        }
    }

    public void updateOddsAfterRoundWinner(Match match, MatchMap currentMap) {
        if (getMinRoundsBeforeMapWin(currentMap.getParticipant1Score(), currentMap.getParticipant2Score()) <= 2) {
            closeMarketsOnCurrentMap(match, currentMap);
        } else {
            updateMapWinnerProbabilitiesBasedOnScore(currentMap);
            matchMapRepository.save(currentMap);

            updateWinnerMatchOdds(match);
            updateWinnerMapOdds(match);
            updateTotalMapsOdds(match);
            updateTotalMapRoundsOdds(match, currentMap);
            updateHandicapOdds(match);
        }
    }

    public void updateOddsAfterMapWinner(Match match, MatchMap currentMap) {
        setResultsToWinnerMapMarkets(match, currentMap);

        updateWinnerMatchOdds(match);
        updateTotalMapsOdds(match);
        updateTotalMapRoundsOdds(match, currentMap);

        setResultsToHandicapMarkets(match);
        setResultsToTotalMapsMarket(match);
    }

    public void updateOddsAfterMatchWinner(Match match, MatchMap currentMap) {
        setResultsToWinnerMapMarkets(match, currentMap);
        setResultsToWinnerMatchMarkets(match);
        updateTotalMapRoundsOdds(match, currentMap);
        setResultsToTotalMapsMarket(match);
        setResultsToHandicapMarkets(match);
        cancelNotCompletedMarkets(match);
    }

    private void updateWinnerMatchOdds(Match match) {
        List<Market> markets = match.getMarkets().stream()
                .filter(m -> m instanceof WinnerMatch)
                .toList();

        List<MatchMap> maps = match.getMatchMaps();

        int[] wins = getMapWins(match);

        int lastIncludedMap = maps.size();
        if (wins[0] - wins[1] != 0) {
            int mapsFinished = maps.stream().filter(m -> m.getWinner() != null).toList().size();
            int needWins = maps.size() / 2 + 1 - mapsFinished;

            double winProbability1 = 0;
            double winProbability2 = 0;
            for (int i = mapsFinished; i < mapsFinished + needWins; i++) {
                MatchMap matchMap = maps.get(i);
                winProbability1 += matchMap.getCurrentWP1();
                winProbability2 += matchMap.getCurrentWP2();
            }

            if ((winProbability1 > winProbability2 && wins[0] > wins[1])
                    || (winProbability2 > winProbability1 && wins[1] > wins[0])) {
                lastIncludedMap = mapsFinished + needWins;
            }
        }

        double winProbability1 = 0;
        double winProbability2 = 0;

        for (int i = 0; i < lastIncludedMap; i++) {
            MatchMap map = maps.get(i);
            winProbability1 += map.getCurrentWP1();
            winProbability2 += map.getCurrentWP2();
        }

        double totalProbability = winProbability1 + winProbability2;
        winProbability1 /= totalProbability;
        winProbability2 /= totalProbability;

        setMarketOddsWith2Outcomes(markets, BigDecimal.ONE, winProbability1, winProbability2);
    }

    private void updateWinnerMapOdds(Match match) {
        List<WinnerMap> markets = match.getMarkets().stream()
                .filter(m -> m instanceof WinnerMap)
                .map(m -> (WinnerMap) m)
                .toList();

        List<MatchMap> matchMaps = match.getMatchMaps();
        if (matchMaps.size() < 2) return;

        for (MatchMap matchMap : matchMaps) {
            if (matchMap.getWinner() != null) continue;
            List<WinnerMap> marketsByMap = markets.stream()
                    .filter(m -> m.getMapNumber().equals(matchMap.getMapNumber())).toList();

            setMarketOddsWith2Outcomes(marketsByMap, BigDecimal.ONE, matchMap.getCurrentWP1(), matchMap.getCurrentWP2());
        }
    }

    private void updateTotalMapsOdds(Match match) {
        BigDecimal marketOutcome = match.getFormat().equals("BO3") ? BigDecimal.valueOf(2.5)
                : match.getFormat().equals("BO5") ? BigDecimal.valueOf(3.5) : null;

        if (marketOutcome == null) return;

        List<Market> overUnderMarkets = match.getMarkets().stream()
                .filter(m -> m instanceof TotalMaps && m.getOutcome().abs().compareTo(marketOutcome) == 0)
                .toList();

        if (overUnderMarkets.getFirst().getResult() != null) return;

        MatchMap map1 = match.getMatchMaps().stream().filter(m -> m.getMapNumber().equals(1)).findFirst().get();
        MatchMap map2 = match.getMatchMaps().stream().filter(m -> m.getMapNumber().equals(2)).findFirst().get();

        double overProbability = map1.getCurrentWP1() > map1.getCurrentWP2() ? map2.getCurrentWP2() : map2.getCurrentWP1();

        setMarketOddsWith2Outcomes(overUnderMarkets, marketOutcome, overProbability, 1.0 - overProbability);
    }

    private void updateTotalMapRoundsOdds(Match match, MatchMap currentMap) {
        List<TotalMapRounds> totalMapRoundsMarkets = match.getMarkets().stream()
                .filter(m -> m instanceof TotalMapRounds &&
                        ((TotalMapRounds) m).getMapNumber().equals(currentMap.getMapNumber()))
                .map(m -> (TotalMapRounds) m)
                .toList();

        int r1 = currentMap.getParticipant1Score();
        int r2 = currentMap.getParticipant2Score();
        int roundsPlayed = r1 + r2;
        int minRoundsBeforeWin = getMinRoundsBeforeMapWin(r1, r2);

        Set<Map.Entry<Integer, Double>> totalRoundsProbabilities = null;

        for (Double overOutcome : totalOverOutcomes) {
            List<TotalMapRounds> pair = totalMapRoundsMarkets.stream()
                    .filter(m -> m.getOutcome().abs().doubleValue() == overOutcome)
                    .sorted(Comparator.comparing(m -> m.getOutcome().doubleValue()))
                    .toList();

            TotalMapRounds under = pair.getFirst();
            TotalMapRounds over = pair.getLast();

            if (under.getResult() != null && over.getResult() != null) continue;

            if (roundsPlayed + minRoundsBeforeWin > overOutcome) {
                setMarketResultsWith2Outcomes(List.of(over, under), List.of(MarketResult.WIN, MarketResult.LOSS));
            } else if (minRoundsBeforeWin == 0 && roundsPlayed < overOutcome) {
                setMarketResultsWith2Outcomes(List.of(over, under), List.of(MarketResult.LOSS, MarketResult.WIN));
            }

            if (over.getClosed()) continue;

            if (roundsPlayed + minRoundsBeforeWin + 2 > overOutcome) {
                closeMarkets(List.of(over, under));
            } else {
                if (totalRoundsProbabilities == null) {
                    Map<Integer, Double> totalRoundsProbabilitiesMap = new HashMap<>();
                    getRoundAmountProbabilities(r1, r2, currentMap.getInitialWP1(), currentMap.getInitialWP2(), totalRoundsProbabilitiesMap);
                    totalRoundsProbabilities = totalRoundsProbabilitiesMap.entrySet();
                }

                double overProbability = 0.0;
                double overValue = over.getOutcome().doubleValue();
                for (Map.Entry<Integer, Double> entry : totalRoundsProbabilities) {
                    if (entry.getKey() > overValue) {
                        overProbability += entry.getValue();
                    }
                }

                setMarketOddsWith2Outcomes(List.of(over, under), over.getOutcome(), overProbability, 1 - overProbability);
            }
        }
    }

    private void updateHandicapOdds(Match match) {
        if (!match.getFormat().equals("BO3")) return;

        List<HandicapMaps> handicapMarkets = match.getMarkets().stream()
                .filter(m -> m instanceof HandicapMaps)
                .map(m -> (HandicapMaps) m).toList();

        List<HandicapMaps> handicapPair1 = List.of(
                handicapMarkets.stream().filter(h -> h.getParticipant().equals(1)
                        && h.getOutcome().compareTo(BigDecimal.valueOf(-1.5)) == 0).findFirst().get(),
                handicapMarkets.stream().filter(h -> h.getParticipant().equals(2)
                        && h.getOutcome().compareTo(BigDecimal.valueOf(1.5)) == 0).findFirst().get()
        );

        List<HandicapMaps> handicapPair2 = List.of(
                handicapMarkets.stream().filter(h -> h.getParticipant().equals(2)
                        && h.getOutcome().compareTo(BigDecimal.valueOf(-1.5)) == 0).findFirst().get(),
                handicapMarkets.stream().filter(h -> h.getParticipant().equals(1)
                        && h.getOutcome().compareTo(BigDecimal.valueOf(1.5)) == 0).findFirst().get()
        );

        Map<Integer, MatchMap> mapsByNumber = getMapsByNumber(match.getMatchMaps());
        MatchMap map1 = mapsByNumber.get(1);
        MatchMap map2 = mapsByNumber.get(2);

        if (handicapPair1.getFirst().getResult() == null) {
            double p1Win2_0 = map1.getCurrentWP1() * map2.getCurrentWP1();
            setMarketOddsWith2Outcomes(handicapPair1, handicapPair1.getFirst().getOutcome(), p1Win2_0, 1 - p1Win2_0);
        }

        if (handicapPair2.getFirst().getResult() == null) {
            double p2Win2_0 = map1.getCurrentWP2() * map2.getCurrentWP2();
            setMarketOddsWith2Outcomes(handicapPair2, handicapPair2.getFirst().getOutcome(), p2Win2_0, 1 - p2Win2_0);
        }
    }

    private <T extends Market> void setMarketOddsWith2Outcomes(List<T> markets, BigDecimal outcome1,
                                                               double winProbability1, double winProbability2) {
        if (markets.size() != 2) {
            log.error("Unable to set odds to markets because list size is invalid {}, should be 2", markets.size());
            return;
        }

        Market m1 = markets.getFirst();
        if (m1.getResult() != null) return;

        boolean closed1 = m1.getClosed();
        BigDecimal odds1 = m1.getOdds();

        for (Market market : markets) {
            if (market.getOutcome().compareTo(outcome1) == 0) {
                market.setOdds(calculateOddsFromProbability(winProbability1));
            } else {
                market.setOdds(calculateOddsFromProbability(winProbability2));
            }
        }

        boolean closeMarket = markets.stream().anyMatch(m -> !isOddsPassesThreshold(m.getOdds()));
        markets.forEach(m -> m.setClosed(closeMarket));

        boolean closed2 = m1.getClosed();
        BigDecimal odds2 = m1.getOdds();

        if (closed2 && !closed1) {
            sendMarketClose(markets);
            marketRepository.saveAll(markets);
        } else if (closed1 && !closed2) {
            sendMarketOpen(markets);
            marketRepository.saveAll(markets);
        } else if (!closed2 && odds1.compareTo(odds2) != 0) {
            sendMarketOddsUpdate(markets);
            marketRepository.saveAll(markets);
        }
    }

    private <T extends Market> void setMarketResultsWith2Outcomes(List<T> markets, List<MarketResult> results) {
        if (markets.size() != results.size() || markets.size() != 2) {
            log.error("Unable to set market results because list sizes are invalid {} - {}, should be 2 - 2",
                    markets.size(), results.size());
            return;
        }

        for (int i = 0; i < 2; i++) {
            Market market = markets.get(i);
            market.setResult(results.get(i));
            market.setClosed(true);
        }

        marketRepository.saveAll(markets);
        sendMarketResult(markets);
    }

    public <T extends Market> void closeMarkets(List<T> markets) {
        for (T market : markets) {
            market.setClosed(true);
        }

        marketRepository.saveAll(markets);
        sendMarketClose(markets);
    }

    private <T extends Market> void cancelMarkets(List<T> markets) {
        for (T market : markets) {
            market.setClosed(true);
            market.setResult(MarketResult.CANCELLED);
        }
        marketRepository.saveAll(markets);
        sendMarketResult(markets);
    }


    // --------------------------- CLOSE HERE ----------------------------
    // --------------------------- CLOSE HERE ----------------------------
    // --------------------------- CLOSE HERE ----------------------------

    private void closeMarketsOnCurrentMap(Match match, MatchMap currentMap) {
        int[] wins = getMapWins(match);
        boolean matchLikelyToEnd = isMatchLikelyToEnd(match, currentMap, wins);

        if (matchLikelyToEnd) {
            closeMatchWinnerMarkets(match);
        } else {
            updateWinnerMatchOdds(match);
        }

        closeMapWinnerMarkets(match, currentMap);
        closeTotalMapsMarkets(match);
        closeTotalMapRoundsMarkets(match, currentMap);
        closeHandicapMarkets(match);
    }

    private void closeMatchWinnerMarkets(Match match) {
        List<Market> markets = match.getMarkets().stream()
                .filter(m -> m instanceof WinnerMatch && !m.getClosed()).toList();
        closeMarkets(markets);
    }

    private void closeMapWinnerMarkets(Match match, MatchMap currentMap) {
        List<Market> markets = match.getMarkets().stream()
                .filter(m -> m instanceof WinnerMap
                        && ((WinnerMap) m).getMapNumber().equals(currentMap.getMapNumber()) && !m.getClosed())
                .toList();

        closeMarkets(markets);
    }

    private void closeTotalMapsMarkets(Match match) {
        List<Market> markets = match.getMarkets().stream()
                .filter(m -> m instanceof TotalMaps && !m.getClosed()).toList();

        closeMarkets(markets);
    }

    private void closeTotalMapRoundsMarkets(Match match, MatchMap currentMap) {
        List<Market> markets = match.getMarkets().stream()
                .filter(m -> m instanceof TotalMapRounds
                        && ((TotalMapRounds) m).getMapNumber().equals(currentMap.getMapNumber()) && !m.getClosed())
                .toList();

        closeMarkets(markets);
    }

    private void closeHandicapMarkets(Match match) {
        List<Market> markets = match.getMarkets().stream()
                .filter(m -> m instanceof HandicapMaps && !m.getClosed()).toList();

        closeMarkets(markets);
    }


    // --------------------------- SET RESULTS HERE ----------------------------
    // --------------------------- SET RESULTS HERE ----------------------------
    // --------------------------- SET RESULTS HERE ----------------------------

    private void setResultsToWinnerMatchMarkets(Match match) {
        List<Market> markets = match.getMarkets().stream()
                .filter(m -> m instanceof WinnerMatch)
                .sorted(Comparator.comparing(m -> m.getOutcome().intValue())).toList();

        Market m1 = markets.getFirst();
        Market m2 = markets.getLast();

        if (match.getWinner() == 1) {
            setMarketResultsWith2Outcomes(List.of(m1, m2), List.of(MarketResult.WIN, MarketResult.LOSS));
        } else {
            setMarketResultsWith2Outcomes(List.of(m1, m2), List.of(MarketResult.LOSS, MarketResult.WIN));
        }
    }

    private void setResultsToWinnerMapMarkets(Match match, MatchMap matchMap) {
        List<Market> mapWinnerMarkets = match.getMarkets().stream()
                .filter(m -> m instanceof WinnerMap && ((WinnerMap) m).getMapNumber().equals(matchMap.getMapNumber()))
                .sorted(Comparator.comparing(m -> m.getOutcome().intValue()))
                .toList();

        Market m1 = mapWinnerMarkets.getFirst();
        Market m2 = mapWinnerMarkets.getLast();

        if (matchMap.getWinner() == 1) {
            matchMap.setCurrentWP1(1.0);
            matchMap.setCurrentWP2(0.0);
            setMarketResultsWith2Outcomes(List.of(m1, m2), List.of(MarketResult.WIN, MarketResult.LOSS));
        } else {
            matchMap.setCurrentWP1(0.0);
            matchMap.setCurrentWP2(1.0);
            setMarketResultsWith2Outcomes(List.of(m1, m2), List.of(MarketResult.LOSS, MarketResult.WIN));
        }

        matchMapRepository.save(matchMap);
    }

    private void setResultsToHandicapMarkets(Match match) {
        if (!match.getFormat().equals("BO3")) return;

        List<HandicapMaps> handicapMarkets = match.getMarkets().stream()
                .filter(m -> m instanceof HandicapMaps)
                .map(m -> (HandicapMaps) m).toList();

        int[] wins = getMapWins(match);

        List<HandicapMaps> handicapPair1 = List.of(
                handicapMarkets.stream().filter(h -> h.getParticipant().equals(1)
                        && h.getOutcome().compareTo(BigDecimal.valueOf(-1.5)) == 0).findFirst().get(),
                handicapMarkets.stream().filter(h -> h.getParticipant().equals(2)
                        && h.getOutcome().compareTo(BigDecimal.valueOf(1.5)) == 0).findFirst().get()
        );

        List<HandicapMaps> handicapPair2 = List.of(
                handicapMarkets.stream().filter(h -> h.getParticipant().equals(1)
                        && h.getOutcome().compareTo(BigDecimal.valueOf(1.5)) == 0).findFirst().get(),
                handicapMarkets.stream().filter(h -> h.getParticipant().equals(2)
                        && h.getOutcome().compareTo(BigDecimal.valueOf(-1.5)) == 0).findFirst().get()
        );

        List<MarketResult> winLoss = List.of(MarketResult.WIN, MarketResult.LOSS);
        List<MarketResult> lossWin = List.of(MarketResult.LOSS, MarketResult.WIN);

        if (wins[0] == 1 && wins[1] == 0) {
            setMarketResultsWith2Outcomes(handicapPair2, winLoss);
        } else if (wins[0] == 0 && wins[1] == 1) {
            setMarketResultsWith2Outcomes(handicapPair1, lossWin);
        } else if (wins[0] == 1 && wins[1] == 1) {
            if (handicapPair1.getFirst().getResult() == null) {
                setMarketResultsWith2Outcomes(handicapPair1, lossWin);
            } else if (handicapPair2.getFirst().getResult() == null) {
                setMarketResultsWith2Outcomes(handicapPair2, winLoss);
            }
        } else if (wins[0] == 2 && wins[1] == 0) {
            setMarketResultsWith2Outcomes(handicapPair1, winLoss);
        } else if (wins[0] == 0 && wins[1] == 2) {
            setMarketResultsWith2Outcomes(handicapPair2, lossWin);
        }
    }

    private void setResultsToTotalMapsMarket(Match match) {
        int mapsPlayed = match.getMatchMaps().stream().filter(m -> m.getWinner() != null).toList().size();
        List<Market> totalMapsMarket = match.getMarkets().stream()
                .filter(m -> m instanceof TotalMaps)
                .toList();

        if (match.getFormat().equals("BO3")) {
            setResultsToTotalMapsMarket(match, mapsPlayed, totalMapsMarket, BigDecimal.valueOf(2.5));
        } else if (match.getFormat().equals("BO5")) {
            setResultsToTotalMapsMarket(match, mapsPlayed, totalMapsMarket, BigDecimal.valueOf(3.5));
            setResultsToTotalMapsMarket(match, mapsPlayed, totalMapsMarket, BigDecimal.valueOf(4.5));
        }
    }

    private void setResultsToTotalMapsMarket(Match match, int mapsPlayed, List<Market> totalMapsMarkets, BigDecimal value) {
        List<Market> underOverMarkets = totalMapsMarkets.stream()
                .filter(m -> m.getOutcome().abs().compareTo(value) == 0)
                .sorted(Comparator.comparing(m -> m.getOutcome().doubleValue()))
                .toList();

        if (underOverMarkets.getFirst().getResult() != null) return;

        if (mapsPlayed > value.doubleValue()
                || (match.getWinner() == null && mapsPlayed > value.subtract(BigDecimal.ONE).doubleValue())) {
            setMarketResultsWith2Outcomes(underOverMarkets, List.of(MarketResult.LOSS, MarketResult.WIN));
        } else if (match.getWinner() != null) {
            setMarketResultsWith2Outcomes(underOverMarkets, List.of(MarketResult.WIN, MarketResult.LOSS));
        }
    }

    private void cancelNotCompletedMarkets(Match match) {
        int mapsPlayed = match.getMatchMaps().stream().filter(m -> m.getWinner() != null).toList().size();
        int totalMaps = Integer.parseInt(match.getFormat().substring(2));

        if (mapsPlayed == totalMaps) return;

        cancelNotCompletedWinnerMapMarkets(match, mapsPlayed);
        cancelNotCompletedTotalMapRoundsMarkets(match, mapsPlayed);
    }

    private void cancelNotCompletedWinnerMapMarkets(Match match, int mapsPlayed) {
        List<Market> markets = match.getMarkets().stream()
                .filter(m -> m instanceof WinnerMap && ((WinnerMap) m).getMapNumber() > mapsPlayed)
                .toList();

        cancelMarkets(markets);
    }

    private void cancelNotCompletedTotalMapRoundsMarkets(Match match, int mapsPlayed) {
        List<Market> markets = match.getMarkets().stream()
                .filter(m -> m instanceof TotalMapRounds && ((TotalMapRounds) m).getMapNumber() > mapsPlayed)
                .toList();

        cancelMarkets(markets);
    }

    // --------------------------- OTHER HERE ----------------------------
    // --------------------------- OTHER HERE ----------------------------
    // --------------------------- OTHER HERE ----------------------------

    private void setMapWinnerProbabilitiesBasedOnMapStatistics(Match match, Map<Integer, ParticipantMapStatistics> mapsStatistics) {
        for (MatchMap map : match.getMatchMaps()) {
            ParticipantMapStatistics statistics = mapsStatistics.get(map.getMapNumber());

            double mapWinRate1 = statistics.getMapsPlayed1() < 3 ? 0.3 : statistics.getWinRate1();
            double mapWinRate2 = statistics.getMapsPlayed2() < 3 ? 0.3 : statistics.getWinRate2();

            double winProbability1 = match.getWinProbability1();
            double winProbability2 = match.getWinProbability2();

            double thresholdStep = 0.05;
            double influencePerStep = 0.02;

            double totalMapWinRate = mapWinRate1 + mapWinRate2;
            double normMapWinRate1 = mapWinRate1 / totalMapWinRate;
            double normMapWinRate2 = mapWinRate2 / totalMapWinRate;

            double mapAdvantage = normMapWinRate1 - normMapWinRate2;

            int steps = (int) (Math.abs(mapAdvantage) / thresholdStep);

            double totalInfluence = Math.min(steps * influencePerStep, 0.1);

            if (mapAdvantage > 0) {
                winProbability1 += totalInfluence;
                winProbability2 -= totalInfluence;
            } else if (mapAdvantage < 0) {
                winProbability1 -= totalInfluence;
                winProbability2 += totalInfluence;
            }

            double total = winProbability1 + winProbability2;

            double wp1 = BigDecimal.valueOf(winProbability1 / total).setScale(3, RoundingMode.HALF_UP).doubleValue();
            double wp2 = BigDecimal.valueOf(winProbability2 / total).setScale(3, RoundingMode.HALF_UP).doubleValue();

            map.setInitialWP1(wp1);
            map.setInitialWP2(wp2);
            map.setCurrentWP1(wp1);
            map.setCurrentWP2(wp2);
        }

        matchMapRepository.saveAll(match.getMatchMaps());
    }

    private double[] calculateRoundWinnerProbabilities(RoundState roundState, boolean team1Ct) {
        double[] baseWin = new double[]{50, 50};

        if (roundState.getCtAlive() == 5 && roundState.getTAlive() == 5) {
            int ctMoney = roundState.getCtMoney() + roundState.getCtGoodWeapons() * 3000;
            int tMoney = roundState.getTMoney() + roundState.getTGoodWeapons() * 3000;

            boolean ctEco = ctMoney < 15000;
            boolean tEco = tMoney < 15000;

            if (ctEco && !tEco) {
                baseWin[0] = 20;
                baseWin[1] = 80;
            } else if (!ctEco && tEco) {
                baseWin[0] = 80;
                baseWin[1] = 20;
            }
        } else {
            int diff = roundState.getCtAlive() - roundState.getTAlive();

            if (diff != 0) {
                baseWin[0] += diff * 10;
                baseWin[1] -= diff * 10;
            }

            if (roundState.getBombPlanted()) {
                baseWin[0] -= 20;
                baseWin[1] += 20;
            }

            baseWin[0] = Math.max(0, Math.min(100, baseWin[0]));
            baseWin[1] = Math.max(0, Math.min(100, baseWin[1]));
        }

        baseWin[0] /= 100;
        baseWin[1] /= 100;

        if (!team1Ct) {
            double temp = baseWin[0];
            baseWin[0] = baseWin[1];
            baseWin[1] = temp;
        }

        return baseWin;
    }

    private void updateMapWinnerProbabilitiesBasedOnScore(MatchMap matchMap) {
        int score1 = matchMap.getParticipant1Score();
        int score2 = matchMap.getParticipant2Score();

        double initialWP1 = matchMap.getInitialWP1();
        double initialWP2 = matchMap.getInitialWP2();

        int diff = score1 - score2;
        if (diff < 0) {
            matchMap.setCurrentWP1(Math.max(0.0, initialWP1 + ROUND_WIN_PERCENT * diff));
            matchMap.setCurrentWP2(Math.min(1.0, initialWP2 - ROUND_WIN_PERCENT * diff));
        } else {
            matchMap.setCurrentWP1(Math.min(1.0, initialWP1 + ROUND_WIN_PERCENT * diff));
            matchMap.setCurrentWP2(Math.max(0.0, initialWP2 - ROUND_WIN_PERCENT * diff));
        }
    }

    private int getMinRoundsBeforeMapWin(int r1, int r2) {
        if (r1 < 12 || r2 < 12) {
            return Math.min(13 - r1, 13 - r2);
        } else {
            int roundsForOT = 24;
            int maxOTRounds = 6;

            int roundsPlayedAfterOT = r1 + r2 - roundsForOT;
            int otNumber = roundsPlayedAfterOT / maxOTRounds;

            int roundsInOtTeam1 = r1 - (roundsForOT / 2) - (maxOTRounds / 2 * otNumber);
            int roundsInOtTeam2 = r2 - (roundsForOT / 2) - (maxOTRounds / 2 * otNumber);

            int needToWinRoundsInOT = 4;
            return Math.min(needToWinRoundsInOT - roundsInOtTeam1, needToWinRoundsInOT - roundsInOtTeam2);
        }
    }

    private int[] getMapWins(Match match) {
        int wins1 = 0;
        int wins2 = 0;

        for (MatchMap matchMap : match.getMatchMaps()) {
            if (matchMap.getWinner() == null) continue;
            if (matchMap.getWinner() == 1) {
                wins1++;
            } else {
                wins2++;
            }
        }

        return new int[]{wins1, wins2};
    }

    private boolean isMatchLikelyToEnd(Match match, MatchMap currentMap, int[] wins) {
        int needWinsToCloseOdds = Integer.parseInt(match.getFormat().substring(2)) / 2;
        int possibleMapWinner = 0;
        if (currentMap.getParticipant1Score() > currentMap.getParticipant2Score()) {
            possibleMapWinner = 1;
        } else if (currentMap.getParticipant1Score() < currentMap.getParticipant2Score()) {
            possibleMapWinner = 2;
        }

        return (needWinsToCloseOdds - wins[0] == 0 && needWinsToCloseOdds - wins[1] == 0)
                || (needWinsToCloseOdds - wins[0] == 0 && possibleMapWinner == 1)
                || (needWinsToCloseOdds - wins[1] == 0 && possibleMapWinner == 2);
    }

    private void getRoundAmountProbabilities(int team1Score, int team2Score, double team1Prob, double team2Prob, Map<Integer, Double> outcomes) {
        class State {
            int t1, t2;
            double prob;

            State(int t1, int t2, double prob) {
                this.t1 = t1;
                this.t2 = t2;
                this.prob = prob;
            }
        }

        Queue<State> queue = new LinkedList<>();
        queue.add(new State(team1Score, team2Score, 1.0));

        while (!queue.isEmpty()) {
            State current = queue.poll();

            int t1 = current.t1;
            int t2 = current.t2;
            double prob = current.prob;

            int totalRounds = t1 + t2;

            if ((t1 >= 13 || t2 >= 13) && Math.abs(t1 - t2) >= 2) {
                outcomes.put(totalRounds, outcomes.getOrDefault(totalRounds, 0.0) + prob);
                continue;
            }

            if (totalRounds >= 24) {
                continue;
            }

            queue.add(new State(t1 + 1, t2, prob * team1Prob));
            queue.add(new State(t1, t2 + 1, prob * team2Prob));
        }
    }

    private Map<Integer, MatchMap> getMapsByNumber(List<MatchMap> matchMaps) {
        Map<Integer, MatchMap> map = new HashMap<>();
        for (MatchMap matchMap : matchMaps) {
            map.put(matchMap.getMapNumber(), matchMap);
        }
        return map;
    }

    // --------------------------- SEND TO SERVER HERE ----------------------------
    // --------------------------- SEND TO SERVER HERE ----------------------------
    // --------------------------- SEND TO SERVER HERE ----------------------------

    private <T extends Market> void sendMarketOddsUpdate(List<T> markets) {
        marketUpdateService.sendMarketUpdate(markets, MarketUpdateType.ODDS);
    }

    private <T extends Market> void sendMarketClose(List<T> markets) {
        marketUpdateService.sendMarketUpdate(markets, MarketUpdateType.CLOSE);
    }

    private <T extends Market> void sendMarketOpen(List<T> markets) {
        marketUpdateService.sendMarketUpdate(markets, MarketUpdateType.OPEN);
    }

    private <T extends Market> void sendMarketResult(List<T> markets) {
        marketUpdateService.sendMarketUpdate(markets, MarketUpdateType.RESULT);
    }

}
