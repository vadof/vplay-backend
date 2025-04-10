package com.vcasino.odds.tracker;

import com.vcasino.odds.entity.Match;
import com.vcasino.odds.entity.MatchMap;
import com.vcasino.odds.entity.enums.MatchStatus;
import com.vcasino.odds.parser.CsMatchParser;
import com.vcasino.odds.parser.MatchParser;
import com.vcasino.odds.repository.MatchMapRepository;
import com.vcasino.odds.repository.MatchRepository;
import com.vcasino.odds.service.CsOddsService;
import com.vcasino.odds.util.MapState;
import com.vcasino.odds.util.ParticipantMapStatistics;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class CsMatchTracker {

    private final Map<String, MatchParser> watchList = new HashMap<>();
    private final CsOddsService oddsService;
    private final MatchMapRepository matchMapRepository;
    private final MatchRepository matchRepository;

    public void trackLiveMatchData(Match match) {
        if (watchList.containsKey(match.getMatchPage())) {
            log.warn("Match#{} already tracking", match.getId());
        }

        try {
            long updateInterval = 500;

            // TODO replace here
            CsMatchParser matchParser = new CsMatchParser("ddd");

            watchList.put(match.getMatchPage(), matchParser);
            log.info("Start tracking Match#{}", match.getId());

            MatchData matchData = new MatchData(matchParser, match, null, null);

            int counter = 290;
            while (true) {
                matchParser.updateMatchPage(counter);

                switch (match.getStatus()) {
                    case LIVE -> handleLive(matchData);
                    case WAITING_TO_START -> handleWaitingToStart(matchData);
                    case BREAK -> handleBreak(matchData);
                    case FINISHED -> {
                        stopTracking(match, false);
                        return;
                    }
                }

                Thread.sleep(updateInterval);

                log.info("Status -> {} | Save_{}", match.getStatus(), counter);
                counter++;
            }
        } catch (Exception e) {
            log.error("Error happened while tracking Match#{}, closing all markets", match.getId(), e);
            stopTracking(match, true);
            throw new RuntimeException(e);
        }
    }

    private void stopTracking(Match match, boolean exception) {
        MatchParser parser = watchList.remove(match.getMatchPage());
        if (parser != null) {
            parser.close();
        }

        if (exception) {
            oddsService.closeMarkets(match.getMarkets());
        }
    }

    private void handleWaitingToStart(MatchData matchData) {
        Match match = matchData.getMatch();
        CsMatchParser matchParser = matchData.getMatchParser();

        if (match.getMatchMaps().isEmpty()) {
            boolean mapsAppeared = matchParser.isMapsAppeared();
            if (mapsAppeared) {
                List<MatchMap> matchMaps = matchParser.getMatchMaps();

                Map<Integer, ParticipantMapStatistics> mapStatistics = new HashMap<>();
                matchParser.setParticipantWinRateAndMapsPlayed(matchMaps, match.getParticipant1(), true, mapStatistics);
                matchParser.setParticipantWinRateAndMapsPlayed(matchMaps, match.getParticipant2(), false, mapStatistics);

                matchMaps.forEach(map -> {
                    map.setMatch(match);
                    map.setMatchId(match.getId());
                });

                match.setMatchMaps(matchMaps);
                oddsService.updateOddsAfterMapsInitialized(match, mapStatistics);
                handleWaitingToStart(matchData);
            }
        } else {
            boolean matchStarted = matchParser.isMatchStarted();
            if (matchStarted) {
                match.setStatus(MatchStatus.LIVE);
                matchRepository.save(match);
                handleLive(matchData);
            }
        }
    }

    private void handleLive(MatchData matchData) {
        CsMatchParser matchParser = matchData.getMatchParser();
        Match match = matchData.getMatch();

        Optional<MapState> optionalMapState = matchParser.getMapState(match.getParticipant1());
        if (optionalMapState.isEmpty()) throw new RuntimeException("Map state is empty");
        MapState mapState = optionalMapState.get();

        Optional<MatchMap> optionalCurrentMap = match.getMatchMaps().stream().filter(map ->
                map.getMapName().equals(mapState.getMapName())).findFirst();
        if (optionalCurrentMap.isEmpty()) throw new RuntimeException("Current map not found");

        MatchMap currentMap = optionalCurrentMap.get();

        Integer mapWinner = getMapWinner(mapState.getCtScore(), mapState.getTScore(), mapState.getIsTeam1CT());
        boolean scoreChanged = setScoreInRightOrder(currentMap, mapState);
        if (mapWinner == null) {
            if (scoreChanged) {
                oddsService.updateOddsAfterRoundWinner(match, currentMap);
            } else {
                oddsService.updateOddsBasedOnRound(match, mapState, currentMap);
            }

        } else {
            currentMap.setWinner(mapWinner);
            matchMapRepository.save(currentMap);

            Integer matchWinner = getMatchWinner(matchData.getMatch());
            if (matchWinner == null) {
                match.setStatus(MatchStatus.BREAK);
                oddsService.updateOddsAfterMapWinner(match, currentMap);
            } else {
                match.setWinner(matchWinner);
                match.setStatus(MatchStatus.FINISHED);
                oddsService.updateOddsAfterMatchWinner(match, currentMap);
            }

            matchRepository.save(match);
        }
    }

    /**
     * @return true if score changed
     */
    private boolean setScoreInRightOrder(MatchMap map, MapState mapState) {
        int ctScore = mapState.getCtScore();
        int tScore = mapState.getTScore();
        int p1Score = map.getParticipant1Score();
        int p2Score = map.getParticipant2Score();

        if (mapState.getIsTeam1CT()) {
            map.setParticipant1Score(ctScore);
            map.setParticipant2Score(tScore);
            return p1Score != ctScore || p2Score != tScore;
        } else {
            map.setParticipant2Score(ctScore);
            map.setParticipant1Score(tScore);
            return p2Score != ctScore || p1Score != tScore;
        }
    }

    private void handleBreak(MatchData matchData) {
        CsMatchParser matchParser = matchData.getMatchParser();

        boolean mapStarted = matchParser.isMapStarted();
        if (mapStarted) {
            Match match = matchData.getMatch();
            match.setStatus(MatchStatus.LIVE);
            matchRepository.save(match);
            handleLive(matchData);
        }
    }

    private Integer getMapWinner(int ctScore, int tScore, boolean isTeam1Ct) {
        Boolean ctWin = null;
        if ((ctScore == 13 && tScore < 12) || (tScore == 13 && ctScore < 12)) {
            ctWin = ctScore == 13;
        } else if (ctScore >= 12 && tScore >= 12) {
            int roundsForOT = 24;
            int maxOTRounds = 6;

            int roundsPlayedAfterOT = ctScore + tScore - roundsForOT;
            int otNumber = roundsPlayedAfterOT / maxOTRounds;

            int roundsInOtTeam1 = ctScore - (roundsForOT / 2) - (maxOTRounds / 2 * otNumber);
            int roundsInOtTeam2 = tScore - (roundsForOT / 2) - (maxOTRounds / 2 * otNumber);

            int needToWinRoundsInOT = 4;
            if (roundsInOtTeam1 >= needToWinRoundsInOT || roundsInOtTeam2 >= needToWinRoundsInOT) {
                ctWin = roundsInOtTeam1 >= needToWinRoundsInOT;
            }
        }

        if (ctWin != null) {
            if (ctWin) {
                return isTeam1Ct ? 1 : 2;
            } else {
                return isTeam1Ct ? 2 : 1;
            }
        }

        return null;
    }

    private Integer getMatchWinner(Match match) {
        int team1Wins = match.getMatchMaps().stream()
                .filter(map -> map.getWinner() != null && map.getWinner() == 1).toList().size();
        int team2Wins = match.getMatchMaps().stream()
                .filter(map -> map.getWinner() != null && map.getWinner() == 2).toList().size();

        int needWins = 2;
        if (match.getFormat().equals("BO5")) {
            needWins = 3;
        }

        if (team1Wins >= needWins) {
            return 1;
        } else if (team2Wins >= needWins) {
            return 2;
        } else {
            return null;
        }
    }

}
