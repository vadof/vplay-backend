package com.vcasino.bet.service.statistics;

import com.vcasino.bet.dto.statistics.BetServiceStatisticsDto;
import com.vcasino.bet.dto.statistics.market.MarketStatisticsByCategory;
import com.vcasino.bet.dto.statistics.market.MarketStatistics;
import com.vcasino.bet.dto.statistics.MatchStatisticsDto;
import com.vcasino.bet.dto.statistics.TournamentStatisticsDto;
import com.vcasino.bet.dto.statistics.user.TopPlayerDto;
import com.vcasino.bet.dto.statistics.user.UserInformationDto;
import com.vcasino.bet.exception.AppException;
import com.vcasino.bet.repository.MatchRepository;
import com.vcasino.bet.repository.StatisticsRepository;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class StatisticsService {

    private final StatisticsRepository repository;
    private final MatchRepository matchRepository;

    public BetServiceStatisticsDto getBetStatistics() {
        return repository.getServiceStatistics();
    }

    public List<TournamentStatisticsDto> getTournamentsStatistics(LocalDate startDate, @Nullable LocalDate endDate) {
        return repository.getTournamentStatistics(startDate, endDate);
    }

    public List<MatchStatisticsDto> getMatchStatistics(@Nullable Integer tournamentId, @Nullable LocalDateTime startDate,
                                                       @Nullable LocalDateTime endDate) {
        if (tournamentId == null && startDate == null) {
            throw new AppException("Tournament id or Start date must be not null", HttpStatus.BAD_REQUEST);
        }

        return repository.getMatchStatistics(tournamentId, startDate, endDate);
    }

    public List<MarketStatisticsByCategory> getMatchMarketsStatistics(Long matchId) {
        if (!matchRepository.existsById(matchId)) {
            throw new AppException("Match#" + matchId + " not found", HttpStatus.NOT_FOUND);
        }
        List<MarketStatistics> marketsStatistics = repository.getMarketsStatistics(matchId);
        return getMarketStatisticsByCategory(marketsStatistics);
    }

    private List<MarketStatisticsByCategory> getMarketStatisticsByCategory(List<MarketStatistics> marketsStatistics) {
        List<MarketStatisticsByCategory> marketsByCategories = new ArrayList<>();
        addWinnerMatchMarkets(marketsStatistics, marketsByCategories);
        addWinnerMapMarkets(marketsStatistics, marketsByCategories);
        addTotalMapsMarkets(marketsStatistics, marketsByCategories);
        addTotalMapRoundsMarkets(marketsStatistics, marketsByCategories);
        addHandicapMarkets(marketsStatistics, marketsByCategories);

        return marketsByCategories;
    }

    private void addWinnerMatchMarkets(List<MarketStatistics> marketsStatistics,
                                       List<MarketStatisticsByCategory> marketsByCategories) {
        List<MarketStatistics> winnerMatchMarkets = marketsStatistics.stream().filter(m -> m.getMarketType().equals("WinnerMatch"))
                .sorted(Comparator.comparingInt(m -> m.getOutcome().intValue()))
                .toList();

        marketsByCategories.add(new MarketStatisticsByCategory("Match Winner", List.of(winnerMatchMarkets)));
    }

    private void addWinnerMapMarkets(List<MarketStatistics> marketsStatistics,
                                     List<MarketStatisticsByCategory> marketsByCategories) {
        List<MarketStatistics> winnerMapMarkets = marketsStatistics.stream().filter(m -> m.getMarketType().equals("WinnerMap"))
                .sorted(Comparator.comparingInt(MarketStatistics::getMapNumber))
                .toList();

        for (int i = 0; i < winnerMapMarkets.size(); i += 2) {
            List<MarketStatistics> mapMarkets = new ArrayList<>(List.of(winnerMapMarkets.get(i), winnerMapMarkets.get(i + 1)));
            mapMarkets.sort(Comparator.comparingInt(m -> m.getOutcome().intValue()));

            marketsByCategories.add(new MarketStatisticsByCategory("Winner. Map " + (i / 2 + 1), List.of(mapMarkets)));
        }
    }

    private void addTotalMapsMarkets(List<MarketStatistics> marketsStatistics,
                                     List<MarketStatisticsByCategory> marketsByCategories) {
        List<MarketStatistics> totalMapsMarkets = marketsStatistics.stream().filter(m -> m.getMarketType().equals("TotalMaps"))
                .sorted(Comparator.comparingInt(m -> m.getOutcome().intValue()))
                .toList();

        marketsByCategories.add(new MarketStatisticsByCategory("Total Maps", List.of(totalMapsMarkets)));
    }

    private void addTotalMapRoundsMarkets(List<MarketStatistics> marketsStatistics,
                                          List<MarketStatisticsByCategory> marketsByCategories) {
        List<MarketStatistics> totalMapRoundsMarkets = marketsStatistics.stream().filter(m -> m.getMarketType().equals("TotalMapRounds"))
                .sorted(Comparator.comparingInt(MarketStatistics::getMapNumber))
                .toList();

        int totalMaps = totalMapRoundsMarkets.stream().map(MarketStatistics::getMapNumber).collect(Collectors.toSet()).size();

        for (int i = 1; i <= totalMaps; i++) {
            int finalMapNumber = i;
            List<MarketStatistics> mapMarkets = totalMapRoundsMarkets.stream()
                    .filter(m -> m.getMapNumber().equals(finalMapNumber))
                    .toList();

            List<MarketStatistics> under = mapMarkets.stream().filter(m -> m.getOutcome().compareTo(BigDecimal.ZERO) < 0)
                    .sorted(Comparator.comparingDouble(m -> m.getOutcome().doubleValue())).toList();
            List<MarketStatistics> over = mapMarkets.stream().filter(m -> m.getOutcome().compareTo(BigDecimal.ZERO) > 0)
                    .sorted(Comparator.comparingDouble(m -> m.getOutcome().doubleValue())).toList();

            List<List<MarketStatistics>> pairs = new ArrayList<>();

            int lastElIdx = under.size() - 1;
            for (int j = 0; j < under.size(); j++) {
                pairs.add(List.of(under.get(lastElIdx - j), over.get(j)));
            }

            marketsByCategories.add(new MarketStatisticsByCategory("Total. Map " + i, pairs));
        }
    }

    private void addHandicapMarkets(List<MarketStatistics> marketsStatistics,
                                    List<MarketStatisticsByCategory> marketsByCategories) {
        List<MarketStatistics> handicapMarkets = marketsStatistics.stream().filter(m -> m.getMarketType().equals("HandicapMaps"))
                .sorted(Comparator.comparingLong(MarketStatistics::getMarketId))
                .toList();

        List<List<MarketStatistics>> pairs = new ArrayList<>();
        for (int i = 0; i < handicapMarkets.size(); i += 2) {
            pairs.add(List.of(handicapMarkets.get(i), handicapMarkets.get(i + 1)));
        }

        marketsByCategories.add(new MarketStatisticsByCategory("Handicap Maps", pairs));
    }

    public List<TopPlayerDto> getTopPlayers() {
        return repository.getTopPlayers();
    }

    public UserInformationDto getUserInformation(Long userId) {
        return repository.getUserInformation(userId);
    }
}
