package com.vcasino.bet.service;

import com.vcasino.bet.client.MarketInitializationRequest;
import com.vcasino.bet.client.OddsClient;
import com.vcasino.bet.dto.RegisterMatchRequest;
import com.vcasino.bet.entity.Match;
import com.vcasino.bet.entity.Tournament;
import com.vcasino.bet.entity.enums.Discipline;
import com.vcasino.bet.entity.enums.MatchStatus;
import com.vcasino.bet.exception.AppException;
import com.vcasino.bet.repository.MatchRepository;
import com.vcasino.bet.repository.ParticipantRepository;
import com.vcasino.bet.repository.TournamentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final TournamentRepository tournamentRepository;
    private final ParticipantRepository participantRepository;
    private final OddsClient oddsClient;

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
            // TODO try to rollback match save if exception
            oddsClient.initializeMarkets(new MarketInitializationRequest(match.getId()));
        } catch (Exception e) {
            log.error("Error happened during markets initialization", e);
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
                .participant1(participantRepository.findByNameAndDiscipline(request.getParticipant1(), discipline)
                        .orElseThrow(() -> new AppException("Participant not found", HttpStatus.NOT_FOUND)))
                .participant2(participantRepository.findByNameAndDiscipline(request.getParticipant2(), discipline)
                        .orElseThrow(() -> new AppException("Participant not found", HttpStatus.NOT_FOUND)))
                .format(getValidatedCsFormat(request.getFormat()))
                .startDate(getValidatedStartDate(request.getStartDate()))
                .status(MatchStatus.WAITING_TO_START)
                .winProbability1(request.getWinProbability1().doubleValue())
                .winProbability2(request.getWinProbability2().doubleValue())
                .startDate(request.getStartDate())
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
        if (startDate.isBefore(LocalDateTime.now().minusMinutes(10))) {
            throw new AppException("It is not possible to add a match earlier than 10 minutes before the start", HttpStatus.BAD_REQUEST);
        }
        return startDate;
    }
}
