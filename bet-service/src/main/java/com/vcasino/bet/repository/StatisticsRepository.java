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
                  COALESCE(SUM(CASE WHEN b.result = 'WIN' THEN (b.amount * b.odds - b.amount) ELSE 0 END), 0) AS totalAmountWin,
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
                        COALESCE(SUM(CASE WHEN b.result = 'WIN' THEN (b.amount * b.odds - b.amount) ELSE 0 END), 0) AS totalAmountWin,
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
                    COALESCE(SUM(CASE WHEN b.result = 'WIN' THEN (b.amount * b.odds - b.amount) ELSE 0 END), 0) AS totalAmountWin,
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
                    COALESCE(SUM(CASE WHEN b.result = 'WIN' THEN (b.amount * b.odds - b.amount) ELSE 0 END), 0) AS totalWinAmount,
                    COALESCE(SUM(CASE WHEN b.result = 'LOSS' THEN (b.amount * b.odds - b.amount) ELSE 0 END), 0) AS totalLossAmount
                FROM
                    bet b
                GROUP BY
                    b.user_id
                ORDER BY
                    totalWinAmount DESC
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
                    COALESCE(SUM(CASE WHEN b.result = 'WIN' THEN (b.amount * b.odds - b.amount) ELSE 0 END), 0) AS totalWinAmount,
                    COALESCE(SUM(CASE WHEN b.result = 'LOSS' THEN b.amount ELSE 0 END), 0) AS totalLossAmount,
                    COUNT(b.bet_id) FILTER (WHERE b.result = 'WIN') AS totalWinBets,
                    COUNT(b.bet_id) FILTER (WHERE b.result = 'LOSS') AS totalLossBets,
                    COUNT(b.bet_id) FILTER (WHERE b.result = 'CANCELLED') AS totalCancelledBets,
                    MAX(b.amount) AS biggestBet,
                    MIN(b.amount) AS smallestBet,
                    AVG(b.amount) AS averageBet,
                    COUNT(DISTINCT t.tournament_id) AS totalTournamentsParticipated,
                    COUNT(DISTINCT m.match_id) AS totalMatchesParticipated
                FROM
                    my_user u
                        LEFT JOIN bet b ON b.user_id = u.user_id
                        LEFT JOIN market ma ON ma.market_id = b.market_id
                        LEFT JOIN match m ON m.match_id = ma.match_id
                        LEFT JOIN tournament t ON t.tournament_id = m.tournament_id
                WHERE
                    u.user_id = ?
                GROUP BY
                    u.user_id;
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

        String latestBetsSql = """
                SELECT b.odds as betOdds,
                       b.amount as betAmount,
                       b.created_at as createdAt,
                       b.result as betResult,
                       ma.outcome as marketOutcome,
                       ma.dtype as marketType,
                       p1.name as matchParticipant1,
                       p2.name as matchParticipant2,
                       t.title as tournamentTitle,
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

        res.setLatestBets(jdbcTemplate.query(latestBetsSql, new BeanPropertyRowMapper<>(UserBetDto.class), userId));

        return res;
    }
}
