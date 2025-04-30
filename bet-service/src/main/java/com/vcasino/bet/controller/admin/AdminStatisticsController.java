package com.vcasino.bet.controller.admin;

import com.vcasino.bet.dto.statistics.BetServiceStatisticsDto;
import com.vcasino.bet.dto.statistics.MatchStatisticsDto;
import com.vcasino.bet.dto.statistics.TournamentStatisticsDto;
import com.vcasino.bet.dto.statistics.market.MarketStatisticsByCategory;
import com.vcasino.bet.dto.statistics.user.TopPlayerDto;
import com.vcasino.bet.dto.statistics.user.UserInformationDto;
import com.vcasino.bet.service.statistics.StatisticsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bet/admin/statistics")
@AllArgsConstructor
@Validated
@Slf4j
public class AdminStatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BetServiceStatisticsDto> getServiceStatistics() {
        log.debug("REST request to get Service Statistics");
        BetServiceStatisticsDto betStatistics = statisticsService.getBetStatistics();
        return ResponseEntity.ok(betStatistics);
    }

    @GetMapping(value = "/tournaments", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TournamentStatisticsDto>> getTournamentsStatistics(
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate) {
        log.debug("REST request to get Tournament Statistics in range {} - {}", startDate, endDate);
        List<TournamentStatisticsDto> res = statisticsService.getTournamentsStatistics(startDate, endDate);
        return ResponseEntity.ok(res);
    }

    @GetMapping(value = "/matches", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MatchStatisticsDto>> getTournamentsStatistics(
            @RequestParam(value = "tournamentId", required = false) Integer tournamentId,
            @RequestParam(value = "startDate", required = false) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) LocalDateTime endDate) {
        log.debug("REST request to get Matches Statistics with params - {} {} {}", tournamentId, startDate, endDate);
        List<MatchStatisticsDto> res = statisticsService.getMatchStatistics(tournamentId, startDate, endDate);
        return ResponseEntity.ok(res);
    }

    @GetMapping(value = "/markets/{matchId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MarketStatisticsByCategory>> getMatchMarketsStatistics(@PathVariable Long matchId) {
        log.debug("REST request to get Match#{} Markets Statistics", matchId);
        List<MarketStatisticsByCategory> res = statisticsService.getMatchMarketsStatistics(matchId);
        return ResponseEntity.ok(res);
    }

    @GetMapping(value = "/users/top", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TopPlayerDto>> getTopPlayers() {
        log.debug("REST request to get top bettors");
        List<TopPlayerDto> res = statisticsService.getTopPlayers();
        return ResponseEntity.ok(res);
    }

    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserInformationDto> getUserInformation(@RequestParam Long userId) {
        log.debug("REST request to get User#{} Statistics", userId);
        UserInformationDto res = statisticsService.getUserInformation(userId);
        return ResponseEntity.ok(res);
    }


}
