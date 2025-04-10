package com.vcasino.odds.service;

import com.vcasino.odds.client.MarketInitializationRequest;
import com.vcasino.odds.entity.Match;
import com.vcasino.odds.entity.enums.Discipline;
import com.vcasino.odds.repository.MatchRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class MarketService {

    private final MatchRepository matchRepository;
    private final CsOddsService csOddsService;

    // TODO remove
    private final TrackingService trackingService;

    public void initializeMarkets(MarketInitializationRequest request) {
        Optional<Match> matchOptional = matchRepository.findById(request.getMatchId());
        if (matchOptional.isEmpty()) {
            throw new RuntimeException("Cannot initialize markets, Match#" + request.getMatchId() + " not found");
        }

        Match match = matchOptional.get();
        if (match.getTournament().getDiscipline().equals(Discipline.COUNTER_STRIKE)) {
            csOddsService.initMatchMarkets(match);
            trackingService.trackUpcomingMatches();
        } else {
            throw new RuntimeException("Cannot initialize markets for this Discipline");
        }
    }

}
