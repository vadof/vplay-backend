package com.vcasino.clickerdata.service;

import com.vcasino.clickerdata.dto.AccountInformation;
import com.vcasino.clickerdata.dto.ChartData;
import com.vcasino.clickerdata.dto.ChartOption;
import com.vcasino.clickerdata.dto.TopAccount;
import com.vcasino.clickerdata.exception.AppException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class AccountStatisticsService {

    private final JdbcTemplate jdbcTemplate;
    private final List<ChartOption> accountClicksOptions = List.of(ChartOption.LAST_WEEK, ChartOption.LAST_MONTH, ChartOption.LAST_3_MONTHS);

    public List<TopAccount> getTop10Accounts() {
        String query = "SELECT id, username, net_worth FROM account ORDER BY net_worth DESC LIMIT 10";

        List<TopAccount> result = new ArrayList<>(10);
        jdbcTemplate.query(query, (rs, rowNum) -> {
            result.add(new TopAccount(rs.getLong("id"),
                    rs.getString("username"),
                    rs.getBigDecimal("net_worth"))
            );
            return null;
        });

        return result;
    }

    public AccountInformation getAccountInformation(Long id, String username) {
        String query = """
                SELECT
                    a.id,
                    a.username,
                    a.level,
                    a.balance_coins AS balanceCoins,
                    a.net_worth AS netWorth,
                    a.passive_earn_per_hour AS passiveEarnPerHour,
                    a.last_sync_date AS lastSyncDate,
                    a.suspicious_actions_number AS suspiciousActionsNumber,
                    a.frozen,
                    (SELECT COUNT(*) FROM account_upgrade au WHERE au.account_id = a.id) AS purchasedUpgrades,
                    s.day AS streak,
                    s.last_received_date AS lastReceivedStreakDay,
                    (SELECT COUNT(*) FROM account_completed_tasks act WHERE act.account_id = a.id) AS completedTasks,
                    (SELECT SUM(ac.amount) FROM account_clicks ac WHERE ac.account_id = a.id) AS totalClicks,
                    best_day.amount AS bestClickDayAmount,
                    best_day.date AS bestClickDayDate
                FROM account a
                LEFT JOIN streak s ON s.account_id = a.id
                LEFT JOIN LATERAL (SELECT ac.amount, ac.date FROM account_clicks ac WHERE ac.account_id = a.id ORDER BY ac.amount DESC LIMIT 1) best_day ON true
                """;

        if (id != null) {
            query += " WHERE a.id = ?";
        } else {
            query += " WHERE a.username = ?";
        }

        List<AccountInformation> results = jdbcTemplate.query(
                query,
                new BeanPropertyRowMapper<>(AccountInformation.class),
                id != null ? id : username
        );

        if (results.isEmpty()) {
            throw new AppException("Account not found", HttpStatus.NOT_FOUND);
        }

        AccountInformation information = results.getFirst();

        information.setClicksChart(getAccountClicksChart(information.getId(), accountClicksOptions.getFirst()));

        return information;
    }

    public ChartData<String, Integer> getAccountClicksChart(Long accountId, ChartOption option) {
        String query = """
                SELECT date, amount
                FROM account_clicks
                WHERE date >= ? AND account_id = ?
                ORDER BY date;
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
            values.add(rs.getInt("amount"));
            return null;
        }, Date.valueOf(startDate), accountId);

        return new ChartData<>(labels, values, accountClicksOptions, option);
    }

}
