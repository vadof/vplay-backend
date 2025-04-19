package com.vcasino.bet.controller;

import com.vcasino.bet.dto.response.MarketsByCategory;
import com.vcasino.bet.dto.response.TournamentDto;
import com.vcasino.bet.service.MatchService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bet/matches")
@AllArgsConstructor
@Validated
@Slf4j
public class MatchController {

    private final MatchService matchService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TournamentDto>> getTournamentsAndItsMatches() {
        log.info("REST request to get Tournaments");
        List<TournamentDto> res = matchService.getTournamentsAndMatches();
        return ResponseEntity.ok(res);
    }

    @GetMapping(value = "/{matchId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MarketsByCategory>> getMatchMarkets(@PathVariable(name = "matchId") Long matchId) {
        log.info("REST request to get Match#{} markets", matchId);
        List<MarketsByCategory> matchMarkets = matchService.getMatchMarkets(matchId);
        return ResponseEntity.ok(matchMarkets);
    }

}
