package com.vcasino.wallet.repository;

import com.vcasino.wallet.dto.statistics.TopWalletDto;
import com.vcasino.wallet.dto.statistics.TransactionDto;
import com.vcasino.wallet.dto.statistics.WalletInformationDto;
import com.vcasino.wallet.exception.AppException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@AllArgsConstructor
public class StatisticsRepository {

    private final JdbcTemplate jdbcTemplate;

    public BigDecimal getTotalWalletsBalance() {
        String query = "SELECT COALESCE(SUM(balance), 0) FROM wallet";
        return jdbcTemplate.queryForObject(query, BigDecimal.class);
    }

    public Long getTotalTransactions() {
        String query = "SELECT COUNT(*) FROM outbox_event WHERE status = 'COMPLETED'";
        return jdbcTemplate.queryForObject(query, Long.class);
    }

    public Map<String, Object> getCurrencyConversionStats(String fromCurrency) {
        String query = """
                SELECT COALESCE(SUM((payload::json ->> 'amount')::DECIMAL), 0) AS total
                FROM outbox_event
                WHERE type = 'CURRENCY_CONVERSION'
                  AND status = 'COMPLETED'
                  AND payload::json ->> 'from' = ?
                """;

        return jdbcTemplate.queryForMap(query, fromCurrency);
    }

    public Map<String, Object> getReservationStats(String type) {
        String query = """
                SELECT COUNT(*) AS count,
                       COALESCE(SUM((payload::json ->> 'amount')::DECIMAL), 0) AS total
                FROM outbox_event
                WHERE type = 'RESERVATION'
                  AND status = 'COMPLETED'
                  AND payload::json ->> 'type' = ?
                """;


        return jdbcTemplate.queryForMap(query, type);
    }

    public WalletInformationDto getWalletInformation(Long id) {
        String query = """
                SELECT
                    w.id,
                    w.balance,
                    w.reserved,
                    w.invited_by as invitedBy,
                    w.updated_at as updatedAt,
                    w.frozen,
                    rb.amount AS referralBonus
                FROM wallet w
                         LEFT JOIN referral_bonus rb ON w.id = rb.referral_id
                WHERE w.id = ?;
                """;

        List<WalletInformationDto> results = jdbcTemplate.query(
                query,
                new BeanPropertyRowMapper<>(WalletInformationDto.class), id
        );

        if (results.isEmpty()) {
            throw new AppException("Wallet not found", HttpStatus.NOT_FOUND);
        }

        WalletInformationDto information = results.getFirst();

        String countQuery = "SELECT COUNT(*) FROM outbox_event WHERE aggregate_id = ?";
        information.setTotalTransactions(jdbcTemplate.queryForObject(countQuery, Long.class, id));

        if (information.getTotalTransactions() == 0) {
            information.setLatestTransactions(new ArrayList<>());
            return information;
        }

        String txQuery = """
                SELECT id, type, payload, status, created_at as createdAt
                FROM outbox_event
                WHERE aggregate_id = ?
                ORDER BY created_at DESC
                LIMIT 10
                """;

        information.setLatestTransactions(
                jdbcTemplate.query(txQuery, new BeanPropertyRowMapper<>(TransactionDto.class), id)
        );

        return information;
    }

    public List<TopWalletDto> getTop10RichestWallets() {
        String query = "SELECT id, balance FROM wallet ORDER BY balance DESC LIMIT 10";
        return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(TopWalletDto.class));
    }
}