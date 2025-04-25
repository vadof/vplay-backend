package com.vcasino.odds.parser;

import com.vcasino.odds.entity.MatchMap;
import com.vcasino.odds.entity.Participant;
import com.vcasino.odds.exception.ParserException;
import com.vcasino.odds.util.MapState;
import com.vcasino.odds.util.ParticipantMapStatistics;
import com.vcasino.odds.util.RoundState;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class CsMatchParser extends MatchParser {

    private final List<String> pistolPrefixes = List.of("cz75", "deagle", "elite", "five", "glock", "hkp2000", "p250", "revolver", "tec9", "usp");
    private Document matchPageDocument;

    public CsMatchParser(String matchPage) throws ParserException {
        super(matchPage);
    }

    @Override
    public void updateMatchPage(boolean updateWithPageRefresh) throws ParserException {
        try {
            if (updateWithPageRefresh) {
                if (mainDriver != null) {
                    mainDriver.close();
                }

                Optional<WebDriver> driverOptional = startDriver(matchPage);
                if (driverOptional.isEmpty()) {
                    throw new Exception("Exception updating match page with refresh " + matchPage);
                }

                mainDriver = driverOptional.get();
            }

            matchPageDocument = getContent(mainDriver, null, "match-page");
        } catch (Exception e) {
            throw new ParserException("Exception happened parsing " + matchPage);
        }
    }

    private Document getContent(WebDriver driver, String contentId, String contentClassName) {
        String html = driver.findElement(contentId != null ? By.id(contentId) : By.className(contentClassName))
                .getAttribute("outerHTML");
        return Jsoup.parse(html);
    }

    public Optional<MapState> getMapState(Participant participant1) {
        Elements scoreboardElements = matchPageDocument.getElementsByClass("scoreboard");
        if (scoreboardElements.isEmpty()) return Optional.empty();

        Element scoreboard = scoreboardElements.getFirst();
        Element topBar = scoreboard.getElementsByClass("topbarBg").getFirst();

        String[] currentRoundText = topBar.getElementsByClass("currentRoundText").text().split(" - ");
        String mapName = currentRoundText[1].substring(0, 1).toUpperCase() + currentRoundText[1].substring(1);

        Integer ctScore = Integer.parseInt(topBar.getElementsByClass("ctScore").text());
        Integer tScore = Integer.parseInt(topBar.getElementsByClass("tScore").text());

        Elements teams = scoreboard.getElementsByClass("team");

        Element ctTeam = teams.getFirst();
        Element tTeam = teams.getLast();

        RoundState roundState = new RoundState();
        setTeamState(roundState, ctTeam.getElementsByTag("tbody").getFirst(), true);
        setTeamState(roundState, tTeam.getElementsByTag("tbody").getFirst(), false);
        roundState.setBombPlanted(topBar.getElementsByAttribute("src").attr("src").contains("exploded"));

        String ctTeamName = ctTeam.getElementsByTag("thead").getFirst().getElementsByClass("teamName").text();

        MapState mapState = MapState.builder()
                .mapName(mapName)
                .ctScore(ctScore)
                .tScore(tScore)
                .isTeam1CT(ctTeamName.equals(participant1.getName()) || ctTeamName.equals(participant1.getShortName()))
                .roundState(roundState)
                .build();

        return Optional.of(mapState);
    }

    private void setTeamState(RoundState roundState, Element teamTable, boolean setForCT) {
        int alive = 5;
        int money = 0;
        int goodWeapons = 0;

        for (Element playerRow : teamTable.getElementsByClass("player")) {
            if (playerRow.classNames().contains("playerDeadText")) {
                alive--;
            } else {
                money += Integer.parseInt(playerRow.getElementsByClass("moneyCell").text().substring(1));

                Elements weaponImg = playerRow.getElementsByClass("weaponCell").getFirst().getElementsByTag("img");
                if (!weaponImg.isEmpty()) {
                    String weaponPng = weaponImg.getFirst().attr("src");
                    weaponPng = weaponPng.substring(weaponPng.lastIndexOf("/") + 1);
                    boolean pistol = false;
                    for (String pistolPrefix : pistolPrefixes) {
                        if (weaponPng.startsWith(pistolPrefix)) {
                            pistol = true;
                            break;
                        }
                    }

                    if (!pistol) {
                        goodWeapons++;
                    }
                }
            }
        }

        if (setForCT) {
            roundState.setCtAlive(alive);
            roundState.setCtMoney(money);
            roundState.setCtGoodWeapons(goodWeapons);
        } else {
            roundState.setTAlive(alive);
            roundState.setTMoney(money);
            roundState.setTGoodWeapons(goodWeapons);
        }
    }

    public List<MatchMap> getMatchMaps() {
        Elements mapElements = matchPageDocument.getElementsByClass("maps").getFirst().getElementsByClass("mapholder");
        List<MatchMap> matchMaps = new ArrayList<>();

        for (int i = 0; i < mapElements.size(); i++) {
            Element mapElement = mapElements.get(i);

            String mapName = mapElement.getElementsByClass("mapname").getFirst().text();
            if (mapName.equals("TBA")) break;

            int team1Score = 0;
            int team2Score = 0;
            Integer winner = null;

            Element mapLeftResults = mapElement.getElementsByClass("results-left").getFirst();
            Element mapRightResults = mapElement.getElementsByClass("results-right").getFirst();

            if (!mapLeftResults.classNames().contains("tie")) {
                team1Score = Integer.parseInt(mapLeftResults.getElementsByClass("results-team-score").getFirst().text());
                team2Score = Integer.parseInt(mapRightResults.getElementsByClass("results-team-score").getFirst().text());
            }

            if (mapLeftResults.classNames().contains("won")) {
                winner = 1;
            } else if (mapRightResults.classNames().contains("won")) {
                winner = 2;
            }

            matchMaps.add(MatchMap.builder()
                    .mapNumber(i + 1)
                    .mapName(mapName)
                    .participant1Score(team1Score)
                    .participant2Score(team2Score)
                    .winner(winner)
                    .build());
        }

        return matchMaps;
    }

    public void setParticipantWinRateAndMapsPlayed(List<MatchMap> matchMaps, Participant p, boolean p1,
                                                   Map<Integer, ParticipantMapStatistics> mapsStatistics) {

        WebDriver participantPageDriver = null;

        try {
            Optional<WebDriver> participantPageDriverOptional = startDriver(p.getParticipantPage());
            if (participantPageDriverOptional.isEmpty()) {
                throw new Exception("Exception initializing participant driver for " + p.getParticipantPage());
            }

            participantPageDriver = participantPageDriverOptional.get();
            Document participantPage = getContent(participantPageDriver, "statsBox", null);

            Elements mapStats = participantPage.getElementsByClass("map-statistics-container");
            for (Element element : mapStats) {
                String mapName = element.getElementsByClass("map-statistics-row-map-mapname").getFirst().text();

                Optional<MatchMap> optionalMatchMap = matchMaps.stream().filter(map -> map.getMapName().equals(mapName)).findFirst();
                if (optionalMatchMap.isEmpty()) continue;

                String winRateText = element.getElementsByClass("map-statistics-row-win-percentage").getFirst().text();
                winRateText = winRateText.substring(0, winRateText.length() - 1);

                Double winRate = new BigDecimal(winRateText).divide(new BigDecimal(100), 3, RoundingMode.DOWN).doubleValue();

                int totalMapsPlayed = 0;
                for (Element mapsPlayed : element.getElementsByClass("highlighted-stat")) {
                    totalMapsPlayed += Integer.parseInt(mapsPlayed.getElementsByClass("stat").getFirst().text());
                }

                MatchMap matchMap = optionalMatchMap.get();
                ParticipantMapStatistics mapStatistics = mapsStatistics.getOrDefault(matchMap.getMapNumber(), new ParticipantMapStatistics());

                if (p1) {
                    mapStatistics.setWinRate1(winRate);
                    mapStatistics.setMapsPlayed1(totalMapsPlayed);
                } else {
                    mapStatistics.setWinRate2(winRate);
                    mapStatistics.setMapsPlayed2(totalMapsPlayed);
                }
                mapsStatistics.put(matchMap.getMapNumber(), mapStatistics);
            }
        } catch (Exception e) {
            log.error("Unable to set map win rate for Participant - {}. Setting win rate to 0", p.getName(), e);

            for (MatchMap matchMap : matchMaps) {
                ParticipantMapStatistics mapStatistics = mapsStatistics.getOrDefault(matchMap.getMapNumber(), new ParticipantMapStatistics());

                if (p1) {
                    mapStatistics.setWinRate1(0.0);
                    mapStatistics.setMapsPlayed1(0);
                } else {
                    mapStatistics.setWinRate2(0.0);
                    mapStatistics.setMapsPlayed2(0);
                }
                mapsStatistics.put(matchMap.getMapNumber(), mapStatistics);
            }
        } finally {
            if (participantPageDriver != null) {
                participantPageDriver.close();
            }
        }
    }

    public boolean isMapsAppeared() {
        Elements mapElements = matchPageDocument.getElementsByClass("maps").getFirst().getElementsByClass("mapholder");

        return mapElements.stream().noneMatch(e -> {
            String mapName = e.getElementsByClass("mapname").getFirst().text();
            return mapName.equals("TBA");
        });
    }

    public boolean isMatchStarted() {
        Elements scoreboardElements = matchPageDocument.getElementsByClass("scoreboard");
        if (scoreboardElements.isEmpty()) {
            return false;
        } else {
            int[] score = getCtAndTScore(scoreboardElements.getFirst());
            return score[0] > 0 || score[1] > 0;
        }
    }

    public boolean isMapStarted() {
        Elements scoreboardElements = matchPageDocument.getElementsByClass("scoreboard");
        if (scoreboardElements.isEmpty()) {
            return false;
        } else {
            int[] score = getCtAndTScore(scoreboardElements.getFirst());
            return (score[0] > 0 || score[1] > 0) && (score[0] < 13 && score[1] < 13);
        }
    }

    private int[] getCtAndTScore(Element scoreboard) {
        Element topBar = scoreboard.getElementsByClass("topbarBg").getFirst();

        int ctScore = Integer.parseInt(topBar.getElementsByClass("ctScore").text());
        int tScore = Integer.parseInt(topBar.getElementsByClass("tScore").text());

        return new int[]{ctScore, tScore};
    }
}
