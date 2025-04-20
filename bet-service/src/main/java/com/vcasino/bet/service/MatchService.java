package com.vcasino.bet.service;

import com.vcasino.bet.client.MarketInitializationRequest;
import com.vcasino.bet.client.OddsClient;
import com.vcasino.bet.dto.response.MarketDto;
import com.vcasino.bet.dto.response.MarketPairDto;
import com.vcasino.bet.dto.response.MarketsByCategory;
import com.vcasino.bet.dto.response.MatchDto;
import com.vcasino.bet.dto.request.RegisterMatchRequest;
import com.vcasino.bet.dto.response.TournamentDto;
import com.vcasino.bet.entity.Match;
import com.vcasino.bet.entity.Tournament;
import com.vcasino.bet.entity.enums.Discipline;
import com.vcasino.bet.entity.enums.MatchStatus;
import com.vcasino.bet.entity.market.Market;
import com.vcasino.bet.entity.market.total.TotalMapRounds;
import com.vcasino.bet.entity.market.winner.WinnerMap;
import com.vcasino.bet.exception.AppException;
import com.vcasino.bet.mapper.MarketMapper;
import com.vcasino.bet.mapper.MatchMapper;
import com.vcasino.bet.mapper.TournamentMapper;
import com.vcasino.bet.repository.MatchRepository;
import com.vcasino.bet.repository.ParticipantRepository;
import com.vcasino.bet.repository.TournamentRepository;
import com.vcasino.commonkafka.event.MatchUpdateEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final TournamentRepository tournamentRepository;
    private final ParticipantRepository participantRepository;
    private final OddsClient oddsClient;

    private final TournamentMapper tournamentMapper;
    private final MatchMapper matchMapper;
    private final MarketMapper marketMapper;
    private final RedisService redisService;

    public Match addMatch(RegisterMatchRequest request) {
        Tournament tournament = tournamentRepository.findById(request.getTournamentId())
                .orElseThrow(() -> new AppException("Tournament not found", HttpStatus.NOT_FOUND));

        if (matchRepository.existsByMatchPage(request.getMatchPage())) {
            throw new AppException("Match already registered", HttpStatus.BAD_REQUEST);
        }

        Match match;
        if (tournament.getDiscipline().equals(Discipline.COUNTER_STRIKE)) {
            match = addCsMatch(tournament, request);
        } else {
            throw new AppException("Unknown discipline", HttpStatus.BAD_REQUEST);
        }

        try {
            oddsClient.initializeMarkets(new MarketInitializationRequest(match.getId()));
        } catch (Exception e) {
            matchRepository.delete(match);
            String errorMessage = "Error happened during markets initialization";
            log.error(errorMessage, e);
            throw new AppException(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return match;
    }

    private Match addCsMatch(Tournament tournament, RegisterMatchRequest request) {
        Discipline discipline = Discipline.COUNTER_STRIKE;

        if (request.getWinProbability1().add(request.getWinProbability2()).compareTo(BigDecimal.ONE) != 0) {
            throw new AppException("The sum of the winning probabilities must be 100%", HttpStatus.BAD_REQUEST);
        }

        Match match = Match.builder()
                .tournament(tournament)
                .matchPage(request.getMatchPage())
                .startDate(getValidatedStartDate(request.getStartDate()))
                .participant1(participantRepository.findByNameAndDiscipline(request.getParticipant1(), discipline)
                        .orElseThrow(() -> new AppException("Participant not found", HttpStatus.NOT_FOUND)))
                .participant2(participantRepository.findByNameAndDiscipline(request.getParticipant2(), discipline)
                        .orElseThrow(() -> new AppException("Participant not found", HttpStatus.NOT_FOUND)))
                .format(getValidatedCsFormat(request.getFormat()))
                .status(MatchStatus.WAITING_TO_START)
                .winProbability1(request.getWinProbability1().doubleValue())
                .winProbability2(request.getWinProbability2().doubleValue())
                .build();

        return matchRepository.save(match);
    }

    private String getValidatedCsFormat(String format) {
        Set<String> supportedFormats = Set.of("BO1", "BO3", "BO5");
        if (supportedFormats.contains(format)) {
            return format;
        } else {
            throw new AppException("Unsupported format - " + format, HttpStatus.BAD_REQUEST);
        }
    }

    private LocalDateTime getValidatedStartDate(LocalDateTime startDate) {
        if (startDate.isBefore(LocalDateTime.now().minusMinutes(15))) {
            throw new AppException("It is not possible to add a match earlier than 15 minutes before the start", HttpStatus.BAD_REQUEST);
        }
        return startDate;
    }

    public List<TournamentDto> getTournamentsAndMatches() {
        List<TournamentDto> cacheResponse = redisService.getTournaments();
        if (cacheResponse != null) {
            return cacheResponse;
        }

        List<Match> matches = matchRepository.findByStartDateAfterAndStatusNot(LocalDateTime.now().minusHours(10), MatchStatus.FINISHED);

        Map<Integer, TournamentDto> tournamentMap = new HashMap<>();
        for (Match match : matches) {
            Tournament matchTournament = match.getTournament();
            TournamentDto tournamentDto = tournamentMap.get(matchTournament.getId());
            if (tournamentDto == null) {
                tournamentDto = tournamentMapper.toDto(matchTournament);
                tournamentDto.setMatches(new ArrayList<>());
            }

            tournamentDto.getMatches().add(matchMapper.toDto(match));

            tournamentMap.put(tournamentDto.getId(), tournamentDto);
        }

        List<TournamentDto> tournamentDtos = tournamentMap.values().stream()
                .toList();

        for (TournamentDto tournamentDto : tournamentDtos) {
            tournamentDto.getMatches().sort(Comparator.comparingLong(MatchDto::getStartDate));
        }

        redisService.cacheTournaments(tournamentDtos);

        return tournamentDtos;
    }

    public List<MarketsByCategory> getMatchMarkets(Long matchId) {
        Match match = matchRepository.findById(matchId).orElseThrow(
                () -> new AppException("Match not found", HttpStatus.NOT_FOUND));

        if (match.getStatus().equals(MatchStatus.FINISHED)) {
            throw new AppException("The match is already over", HttpStatus.BAD_REQUEST);
        }

        String p1Name = match.getParticipant1().getName();
        String p2Name = match.getParticipant2().getName();

        List<Market> markets = match.getMarkets();

        int totalMaps = Integer.parseInt(match.getFormat().substring(2));

        List<MarketsByCategory> marketsByCategories = new ArrayList<>();
        addWinnerMatchMarkets(markets, p1Name, p2Name, marketsByCategories);
        addWinnerMapMarkets(markets, p1Name, p2Name, marketsByCategories);
        addTotalMapsMarkets(markets, p1Name, p2Name, marketsByCategories);
        addTotalMapRoundsMarkets(markets, p1Name, p2Name, totalMaps, marketsByCategories);
        addHandicapMarkets(markets, p1Name, p2Name, marketsByCategories);

        return marketsByCategories;
    }

    public void handleMatchUpdateEvent(MatchUpdateEvent event) {
        Optional<Match> matchOptional = matchRepository.findById(event.matchId());
        if (matchOptional.isEmpty()) {
            log.error("Match#{} not found", event.matchId());
            return;
        }

        Match match = matchOptional.get();

        if (event.matchEnded()) {
            log.info("Match#{} ended", event.matchId());
            redisService.removeMatchFromCache(match.getId());
            redisService.publishUpdatedMatchEvent(match.getId(), null, null, true);
        } else if (event.scoreUpdated()) {
            log.info("Match#{} score updated", event.matchId());
            redisService.publishUpdatedMatchEvent(match.getId(), null, matchMapper.getMatchMaps(match), false);
        }
    }

    private void addWinnerMatchMarkets(List<Market> markets, String p1Name, String p2Name,
                                                    List<MarketsByCategory> marketsByCategories) {
        List<Market> winnerMatchMarkets = markets.stream().filter(m -> m.getType().equals("WinnerMatch"))
                .sorted(Comparator.comparingInt(m -> m.getOutcome().intValue()))
                .toList();

        List<MarketDto> marketDtos = marketMapper.toDtos(winnerMatchMarkets, p1Name, p2Name);
        MarketPairDto marketPair = new MarketPairDto(marketDtos, marketDtos.getFirst().getClosed());

        marketsByCategories.add(new MarketsByCategory("Match Winner", List.of(marketPair)));
    }

    private void addWinnerMapMarkets(List<Market> markets, String p1Name, String p2Name,
                                                        List<MarketsByCategory> marketsByCategories) {
        List<Market> winnerMapMarkets = markets.stream().filter(m -> m.getType().equals("WinnerMap"))
                .sorted(Comparator.comparingInt(m -> ((WinnerMap )m).getMapNumber()))
                .toList();

        int mapNumber = 1;
        for (int i = 0; i < winnerMapMarkets.size(); i += 2) {
            List<Market> mapMarkets = new ArrayList<>(List.of(winnerMapMarkets.get(i), winnerMapMarkets.get(i + 1)));
            mapMarkets.sort(Comparator.comparingInt(m -> m.getOutcome().intValue()));

            List<MarketDto> marketDtos = marketMapper.toDtos(List.of(winnerMapMarkets.get(i), winnerMapMarkets.get(i + 1)), p1Name, p2Name);
            MarketPairDto marketPair = new MarketPairDto(marketDtos, marketDtos.getFirst().getClosed());

            marketsByCategories.add(new MarketsByCategory("Winner. Map " + mapNumber, List.of(marketPair)));
            mapNumber++;
        }
    }

    private void addTotalMapsMarkets(List<Market> markets, String p1Name, String p2Name,
                                     List<MarketsByCategory> marketsByCategories) {
        List<Market> totalMapsMarkets = markets.stream().filter(m -> m.getType().equals("TotalMaps"))
                .sorted(Comparator.comparingInt(m -> m.getOutcome().intValue()))
                .toList();

        List<MarketDto> marketDtos = marketMapper.toDtos(totalMapsMarkets, p1Name, p2Name);
        MarketPairDto marketPair = new MarketPairDto(marketDtos, marketDtos.getFirst().getClosed());

        marketsByCategories.add(new MarketsByCategory("Total Maps", List.of(marketPair)));
    }

    private void addTotalMapRoundsMarkets(List<Market> markets, String p1Name, String p2Name, int totalMaps,
                                     List<MarketsByCategory> marketsByCategories) {
        List<Market> totalMapRoundsMarkets = markets.stream().filter(m -> m.getType().equals("TotalMapRounds"))
                .sorted(Comparator.comparingInt(m -> ((TotalMapRounds) m).getMapNumber()))
                .toList();

        for (int i = 1; i <= totalMaps; i++) {
            int finalMapNumber = i;
            List<Market> mapMarkets = totalMapRoundsMarkets.stream()
                    .filter(m -> ((TotalMapRounds) m).getMapNumber().equals(finalMapNumber))
                    .toList();

            List<Market> under = mapMarkets.stream().filter(m -> m.getOutcome().compareTo(BigDecimal.ZERO) < 0)
                    .sorted(Comparator.comparingDouble(m -> m.getOutcome().doubleValue())).toList();
            List<Market> over = mapMarkets.stream().filter(m -> m.getOutcome().compareTo(BigDecimal.ZERO) > 0)
                    .sorted(Comparator.comparingDouble(m -> m.getOutcome().doubleValue())).toList();

            List<MarketPairDto> pairs = new ArrayList<>();
            int lastElIdx = under.size() - 1;
            for (int j = 0; j < under.size(); j++) {
                List<MarketDto> pair = marketMapper.toDtos(List.of(under.get(lastElIdx - j), over.get(j)), p1Name, p2Name);
                pairs.add(new MarketPairDto(pair, pair.getFirst().getClosed()));
            }

            marketsByCategories.add(new MarketsByCategory("Total. Map " + i, pairs));
        }
    }

    private void addHandicapMarkets(List<Market> markets, String p1Name, String p2Name,
                                     List<MarketsByCategory> marketsByCategories) {
        List<Market> handicapMarkets = markets.stream().filter(m -> m.getType().equals("HandicapMaps"))
                .sorted(Comparator.comparingLong(Market::getId))
                .toList();

        List<MarketPairDto> pairs = new ArrayList<>();
        for (int i = 0; i < handicapMarkets.size(); i += 2) {
            List<MarketDto> pair = marketMapper.toDtos(List.of(handicapMarkets.get(i), handicapMarkets.get(i + 1)), p1Name, p2Name);
            pairs.add(new MarketPairDto(pair, pair.getFirst().getClosed()));
        }

        marketsByCategories.add(new MarketsByCategory("Handicap Maps", pairs));
    }
}
