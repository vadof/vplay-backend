package com.vcasino.bet.repository;

import com.vcasino.bet.dto.statistics.AdditionalMatchStatistics;
import com.vcasino.bet.dto.statistics.BetServiceStatisticsDto;
import com.vcasino.bet.dto.statistics.market.MarketStatistics;
import com.vcasino.bet.dto.statistics.MatchStatisticsDto;
import com.vcasino.bet.dto.statistics.TournamentStatisticsDto;
import com.vcasino.bet.dto.statistics.user.TopPlayerDto;
import com.vcasino.bet.dto.statistics.user.UserBetDto;
import com.vcasino.bet.dto.statistics.user.UserInformationDto;
import com.vcasino.bet.exception.AppException;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@AllArgsConstructor
public class StatisticsRepository {

    private final JdbcTemplate jdbcTemplate;

    public BetServiceStatisticsDto getServiceStatistics() {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM bet) AS totalBets,
                    (SELECT COUNT(*) FROM bet WHERE created_at >= CURRENT_DATE) AS betsToday,
                    (SELECT COUNT(*) FROM bet WHERE created_at >= NOW() - INTERVAL '1 WEEK') AS betsLastWeek,
                    (SELECT COUNT(*) FROM bet WHERE created_at >= NOW() - INTERVAL '1 MONTH') AS betsLastMonth,
                    (SELECT COALESCE(SUM(amount), 0) FROM bet) AS totalAmountWagered,
                    (SELECT COALESCE(SUM(amount * odds - amount), 0) FROM bet WHERE result = 'WIN') AS totalAmountWin,
                    (SELECT COALESCE(SUM(amount), 0) FROM bet WHERE result = 'LOSS') AS totalAmountLoss
                """;

        BetServiceStatisticsDto stats = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(BetServiceStatisticsDto.class));
        stats.setTotalServiceProfit(stats.getTotalAmountLoss().subtract(stats.getTotalAmountWin()));
        return stats;
    }

    public List<TournamentStatisticsDto> getTournamentStatistics(LocalDate startDate, @Nullable LocalDate endDate) {
        String whereSql = "WHERE t.start_date >= ?";
        Object[] args;
        if (endDate != null) {
            args = new Object[]{startDate, endDate};
            whereSql += " AND t.end_date <= ?";
        } else {
            args = new Object[]{startDate};
        }
        String sql = """
                SELECT
                   t.tournament_id AS tournamentId,
                   t.title,
                   t.discipline,
                   t.tournament_page AS tournamentPage,
                   t.image_s3_key AS image,
                   COUNT(DISTINCT ma.match_id) AS matchCount,
                   t.start_date AS startDate,
                   t.end_date AS endDate,
                   COUNT(DISTINCT b.bet_id) AS betCount,
                   COALESCE(SUM(b.amount), 0) AS totalAmountWagered,
                   ROUND(COALESCE(SUM(CASE WHEN b.result = 'WIN' THEN (b.amount * b.odds - b.amount) ELSE 0 END), 0), 2) AS totalAmountWin,
                   COALESCE(SUM(CASE WHEN b.result = 'LOSS' THEN b.amount ELSE 0 END), 0) AS totalAmountLoss
                FROM tournament t
                         LEFT JOIN match ma ON ma.tournament_id = t.tournament_id
                         LEFT JOIN market m ON m.match_id = ma.match_id
                         LEFT JOIN bet b ON b.market_id = m.market_id
                %s
                GROUP BY t.tournament_id, t.title, t.discipline, t.tournament_page, t.image_s3_key, t.start_date, t.end_date
                ORDER BY t.start_date;
                """.formatted(whereSql);

        List<TournamentStatisticsDto> stats = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(TournamentStatisticsDto.class), args);

        for (TournamentStatisticsDto stat : stats) {
            stat.setProfit(stat.getTotalAmountLoss().subtract(stat.getTotalAmountWin()));
        }

        return stats;
    }

    public List<MatchStatisticsDto> getMatchStatistics(Integer tournamentId, LocalDateTime startDate, @Nullable LocalDateTime endDate) {
        String whereSql;
        Object[] args;

        if (tournamentId != null) {
            whereSql = "WHERE t.tournament_id = ?";
            args = new Object[]{tournamentId};
        } else {
            whereSql = "WHERE m.start_date >= ?";
            if (endDate == null) {
                args = new Object[]{startDate};
            } else {
                whereSql += " AND m.start_date <= ?";
                args = new Object[]{startDate, endDate};
            }
        }

        String sql = """
                SELECT
                    m.match_id as matchId,
                    m.match_page as matchPage,
                    m.format,
                    m.start_date as startDate,
                    m.status,
                    m.winner,
                    m.win_probability1 as winProbability1,
                    m.win_probability2 as winProbability2,
                    t.title as tournamentTitle,
                    t.tournament_page as tournamentPage,
                    t.discipline,
                    p1.name as participant1Name,
                    p2.name as participant2Name
                FROM match m
                         JOIN tournament t ON t.tournament_id = m.tournament_id
                         JOIN participant p1 ON p1.participant_id = m.participant1_id
                         JOIN participant p2 ON p2.participant_id = m.participant2_id
                %s
                ORDER BY m.start_date
                """.formatted(whereSql);

        List<MatchStatisticsDto> stats = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(MatchStatisticsDto.class), args);

        for (MatchStatisticsDto stat : stats) {
            String additionalSql = """
                    SELECT
                        COUNT(DISTINCT b.bet_id) AS betCount,
                        COALESCE(SUM(b.amount), 0) AS totalAmountWagered,
                        ROUND(COALESCE(SUM(CASE WHEN b.result = 'WIN' THEN (b.amount * b.odds - b.amount) ELSE 0 END), 0), 2) AS totalAmountWin,
                        COALESCE(SUM(CASE WHEN b.result = 'LOSS' THEN b.amount ELSE 0 END), 0) AS totalAmountLoss,
                        COUNT(*) FILTER (WHERE b.result = 'WIN') AS totalWinBets,
                        COUNT(*) FILTER (WHERE b.result = 'LOSS') AS totalLossBets,
                        COUNT(*) FILTER (WHERE b.result = 'CANCELLED') AS totalCancelledBets
                    FROM match m
                             LEFT JOIN market ma ON ma.match_id = m.match_id
                             LEFT JOIN bet b ON b.market_id = ma.market_id
                    WHERE m.match_id = ?;
                    """;

            AdditionalMatchStatistics additionalStats = jdbcTemplate.queryForObject(additionalSql,
                    new BeanPropertyRowMapper<>(AdditionalMatchStatistics.class), stat.getMatchId());
            additionalStats.setProfit(additionalStats.getTotalAmountLoss().subtract(additionalStats.getTotalAmountLoss()));

            stat.setAdditionalStatistics(additionalStats);
        }

        return stats;
    }

    public List<MarketStatistics> getMarketsStatistics(Long matchId) {
        String sql = """
                SELECT
                    m.match_id as matchId,
                    m.market_id as marketId,
                    m.dtype as marketType,
                    m.outcome,
                    m.closed,
                    m.participant,
                    m.map_number as mapNumber,
                    m.result,
                    COUNT(b.bet_id) AS betCount,
                    COALESCE(SUM(b.amount), 0) AS totalAmountWagered,
                    ROUND(COALESCE(SUM(CASE WHEN b.result = 'WIN' THEN (b.amount * b.odds - b.amount) ELSE 0 END), 0), 2) AS totalAmountWin,
                    COALESCE(SUM(CASE WHEN b.result = 'LOSS' THEN b.amount ELSE 0 END), 0) AS totalAmountLoss,
                    ROUND(COALESCE(AVG(b.amount), 0), 2) AS averageBetAmount,
                    COALESCE(MAX(b.amount), 0) AS maxBetAmount
                FROM market m
                         LEFT JOIN bet b ON b.market_id = m.market_id
                WHERE m.match_id = ?
                GROUP BY m.market_id;
                """;

        return jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(MarketStatistics.class), matchId);
    }

    public List<TopPlayerDto> getTopPlayers() {
        String sql = """
                SELECT
                    b.user_id as userId,
                    ROUND(COALESCE(SUM(CASE WHEN b.result = 'WIN' THEN (b.amount * b.odds - b.amount) ELSE 0 END), 0), 2) AS totalWinAmount,
                    ROUND(COALESCE(SUM(CASE WHEN b.result = 'LOSS' THEN (b.amount * b.odds - b.amount) ELSE 0 END), 0), 2) AS totalLossAmount
                FROM bet b
                GROUP BY b.user_id
                ORDER BY totalWinAmount DESC
                LIMIT 20;
                """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(TopPlayerDto.class));
    }

    public UserInformationDto getUserInformation(Long userId) {
        String mainSql = """
                SELECT
                    u.user_id,
                    COUNT(DISTINCT b.bet_id) AS totalBetsPlaced,
                    COALESCE(SUM(b.amount), 0) AS totalAmountWagered,
                    ROUND(COALESCE(SUM(CASE WHEN b.result = 'WIN' THEN (b.amount * b.odds - b.amount) ELSE 0 END), 0), 2) AS totalWinAmount,
                    COALESCE(SUM(CASE WHEN b.result = 'LOSS' THEN b.amount ELSE 0 END), 0) AS totalLossAmount,
                    COUNT(b.bet_id) FILTER (WHERE b.result = 'WIN') AS totalWinBets,
                    COUNT(b.bet_id) FILTER (WHERE b.result = 'LOSS') AS totalLossBets,
                    COUNT(b.bet_id) FILTER (WHERE b.result = 'CANCELLED') AS totalCancelledBets,
                    MAX(b.amount) AS biggestBet,
                    MIN(b.amount) AS smallestBet,
                    ROUND(AVG(b.amount), 2) AS averageBet,
                    COUNT(DISTINCT t.tournament_id) AS totalTournamentsParticipated,
                    COUNT(DISTINCT m.match_id) AS totalMatchesParticipated
                FROM
                    my_user u
                        LEFT JOIN bet b ON b.user_id = u.user_id
                        LEFT JOIN market ma ON ma.market_id = b.market_id
                        LEFT JOIN match m ON m.match_id = ma.match_id
                        LEFT JOIN tournament t ON t.tournament_id = m.tournament_id
                WHERE u.user_id = ?
                GROUP BY u.user_id;
                """;

        List<UserInformationDto> resList = jdbcTemplate.query(mainSql, new BeanPropertyRowMapper<>(UserInformationDto.class), userId);
        if (resList.isEmpty()) {
            throw new AppException("User#" + userId + " not found", HttpStatus.NOT_FOUND);
        }

        UserInformationDto res = resList.getFirst();

        int totalBetsPlaceWithoutCancelled = res.getTotalBetsPlaced() - res.getTotalCancelledBets();
        BigDecimal winPercentage;
        if (totalBetsPlaceWithoutCancelled == 0) {
            winPercentage = BigDecimal.ONE;
        } else {
            winPercentage = BigDecimal.valueOf(res.getTotalWinBets() / (double) totalBetsPlaceWithoutCancelled)
                    .multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP);
        }

        res.setWinPercentage(winPercentage);

        res.setLatestBets(getUserBets(userId));

        return res;
    }

    private List<UserBetDto> getUserBets(Long userId) {
        String latestBetsSql = """
                SELECT b.odds as bet_odds,
                       b.amount as bet_amount,
                       b.created_at as created_at,
                       b.result as bet_result,
                       ma.outcome as market_outcome,
                       ma.dtype as market_type,
                       ma.map_number as map_number,
                       ma.participant as market_participant,
                       p1.name as match_participant1,
                       p2.name as match_participant2,
                       t.title as tournament_title,
                       t.discipline as discipiline
                FROM
                    bet b
                        LEFT JOIN market ma ON ma.market_id = b.market_id
                        LEFT JOIN match m ON m.match_id = ma.match_id
                        LEFT JOIN participant p1 ON p1.participant_id = m.participant1_id
                        LEFT JOIN participant p2 ON p2.participant_id = m.participant2_id
                        LEFT JOIN tournament t ON t.tournament_id = m.tournament_id
                WHERE b.user_id = ?
                ORDER BY b.created_at DESC
                LIMIT 10;
                """;

        return jdbcTemplate.query(latestBetsSql, (rs, rowNum) -> {
            BigDecimal outcome = rs.getBigDecimal("market_outcome");
            String marketType = rs.getString("market_type");
            String p1Name = rs.getString("match_participant1");
            String p2Name = rs.getString("match_participant2");
            Integer participant = rs.getInt("market_participant");
            Integer mapNumber = rs.getInt("map_number");

            String marketOutcome = getMarketOutcomeWithCategory(outcome, marketType, p1Name, p2Name, participant, mapNumber);
            String matchDescription = rs.getString("tournament_title") + ". " +
                    rs.getString("discipiline") + ". " + p1Name + " VS " + p2Name;

            return UserBetDto.builder()
                    .betOdds(rs.getBigDecimal("bet_odds"))
                    .betAmount(rs.getBigDecimal("bet_amount"))
                    .createdAt(rs.getString("created_at"))
                    .betResult(rs.getString("bet_result"))
                    .marketOutcome(marketOutcome)
                    .matchDescription(matchDescription)
                    .build();
        }, userId);
    }

    private String getMarketOutcomeWithCategory(BigDecimal outcome, String marketType,
                                                String p1Name, String p2Name,
                                                Integer participant, Integer mapNumber) {
        switch (marketType) {
            case "WinnerMatch" -> {
                return "Match Winner. " + (outcome.compareTo(BigDecimal.ONE) == 0 ? p1Name : p2Name);
            }
            case "WinnerMap" -> {
                return "Winner Map " + mapNumber + "." + (outcome.compareTo(BigDecimal.ONE) == 0 ? p1Name : p2Name);
            }
            case "TotalMaps" -> {
                return "Total Maps. " + mapNumber + "." + (outcome.compareTo(BigDecimal.ZERO) < 0 ?
                        "Under " + outcome.abs() : "Over " + outcome);
            }
            case "TotalMapRounds" -> {
                return "Total Map " + mapNumber + ". " + (outcome.compareTo(BigDecimal.ZERO) < 0 ?
                        "Under " + outcome.abs() : "Over " + outcome);
            }
            case "HandicapMaps" -> {
                String participantName = participant == 1 ? p1Name : p2Name;
                return "Handicap Maps. " + participantName + " " +
                        (outcome.compareTo(BigDecimal.ZERO) < 0 ? outcome.toString() : "+" + outcome + " " + outcome);
            }
        }

        return outcome.toString();
    }
}
