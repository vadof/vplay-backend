package com.vcasino.clickerdata.service;

import com.vcasino.clickerdata.dto.ChartData;
import com.vcasino.clickerdata.dto.ChartOption;
import com.vcasino.clickerdata.dto.GeneralStatistics;
import com.vcasino.clickerdata.exception.AppException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class GeneralStatisticsService {

    private final JdbcTemplate jdbcTemplate;
    private final List<ChartOption> activeUsersOptions = List.of(ChartOption.LAST_WEEK, ChartOption.LAST_MONTH, ChartOption.LAST_3_MONTHS);
    private final List<ChartOption> clicksOptions = List.of(ChartOption.TODAY, ChartOption.LAST_WEEK, ChartOption.LAST_MONTH);

    public GeneralStatistics getGeneralStatistics() {
        GeneralStatistics statistics = getGeneralStatisticsWithoutChartData();
        statistics.setActiveUsersChart(getActiveUsersChart(activeUsersOptions.getFirst()));
        statistics.setTotalClicksChart(getTotalClicksChart(clicksOptions.getFirst()));
        statistics.setLevelPercentageChart(getLevelPercentageChart());

        return statistics;
    }

    private GeneralStatistics getGeneralStatisticsWithoutChartData() {
        String query = """
                SELECT
                    (SELECT COUNT(*) FROM account_clicks WHERE date = CURRENT_DATE) as activeUsersToday,
                    (SELECT SUM(amount) FROM account_clicks WHERE date = CURRENT_DATE) as clicksToday,
                    (SELECT ROUND(SUM(amount) / (SELECT COUNT(DISTINCT account_id) FROM account_clicks), 0) FROM total_clicks) as clicksPerUser,
                    (SELECT SUM(suspicious_actions_number) FROM account WHERE suspicious_actions_number > 0) as suspiciousActivityCount,
                    (SELECT COUNT(*) FROM account WHERE frozen = TRUE) as frozenAccounts,
                    (SELECT COUNT(*) FROM streak WHERE last_received_date = CURRENT_DATE) as streaksTakenToday,
                    (SELECT SUM(net_worth) FROM account) as totalNetWorth,
                    (SELECT COUNT(*) FROM account_upgrade) as totalUpgradesPurchased;
                """;

        return jdbcTemplate.queryForObject(query, new BeanPropertyRowMapper<>(GeneralStatistics.class));
    }

    public ChartData<String, Integer> getActiveUsersChart(ChartOption option) {
        String query = """
                SELECT date, COUNT(account_id) AS users
                FROM account_clicks
                WHERE date >= ?
                GROUP BY date
                ORDER BY date
                """;

        LocalDate startDate = switch (option) {
            case LAST_WEEK -> LocalDate.now().minusWeeks(1);
            case LAST_MONTH -> LocalDate.now().minusMonths(1);
            case LAST_3_MONTHS -> LocalDate.now().minusMonths(3);
            default -> throw new AppException("Unsupported ChartOption: " + option, HttpStatus.BAD_REQUEST);
        };

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        jdbcTemplate.query(query, (rs, rowNum) -> {
            labels.add(rs.getString("date"));
            values.add(rs.getInt("users"));
            return null;
        }, Date.valueOf(startDate));

        return new ChartData<>(labels, values, activeUsersOptions, option);
    }

    public ChartData<String, Long> getTotalClicksChart(ChartOption option) {
        String query = """
                SELECT TO_CHAR(date, 'HH24') AS time, SUM(amount) AS amount
                FROM total_clicks
                WHERE date >= ?
                GROUP BY TO_CHAR(date, 'HH24')
                ORDER BY time;
                """;

        LocalDateTime startDateTime = switch (option) {
            case TODAY -> LocalDateTime.now().toLocalDate().atStartOfDay();
            case LAST_WEEK -> LocalDateTime.now().minusWeeks(1);
            case LAST_MONTH -> LocalDateTime.now().minusMonths(1);
            default -> throw new AppException("Unsupported ChartOption: " + option, HttpStatus.BAD_REQUEST);
        };

        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        jdbcTemplate.query(query, (rs, rowNum) -> {
            labels.add(rs.getString("time"));
            values.add(rs.getLong("amount"));
            return null;
        }, Timestamp.valueOf(startDateTime));

        return new ChartData<>(labels, values, clicksOptions, option);
    }

    public ChartData<Integer, Double> getLevelPercentageChart() {
        String query = "SELECT a.level, COUNT(*) AS count FROM account a GROUP BY a.level ORDER BY a.level";

        List<Integer> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        jdbcTemplate.query(query,
                (rs, rowNum) -> {
                    labels.add(rs.getInt("level"));
                    values.add(rs.getInt("count"));
                    return null;
                });

        int totalCount = values.stream().mapToInt(Integer::intValue).sum();
        List<Double> percentageValues = values.stream()
                .map(count -> (totalCount == 0) ? 0 : Math.round((count / (double) totalCount) * 100 * 100.0) / 100.0)
                .toList();


        return new ChartData<>(labels, percentageValues, Collections.emptyList(), null);
    }
}
