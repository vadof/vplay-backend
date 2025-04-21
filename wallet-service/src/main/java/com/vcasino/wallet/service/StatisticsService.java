package com.vcasino.wallet.service;

import com.vcasino.wallet.dto.statistics.ServiceStatisticsDto;
import com.vcasino.wallet.dto.statistics.WalletInformationDto;
import com.vcasino.wallet.repository.StatisticsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
@Slf4j
public class StatisticsService {

    private final StatisticsRepository repository;

    public ServiceStatisticsDto getStatistics() {
        ServiceStatisticsDto response = new ServiceStatisticsDto();

        response.setTotalWalletsBalance(repository.getTotalWalletsBalance());
        response.setTotalTransactions(repository.getTotalTransactions());

        var vDollarStats = repository.getCurrencyConversionStats("VDollar");
        response.setVDollarToVCoinAmount((BigDecimal) vDollarStats.get("total"));

        var vCoinStats = repository.getCurrencyConversionStats("VCoin");
        response.setVCoinToVDollarAmount((BigDecimal) vCoinStats.get("total"));

        var depositStats = repository.getReservationStats("DEPOSIT");
        response.setDepositCount(((Number) depositStats.get("count")).longValue());
        response.setDepositAmount((BigDecimal) depositStats.get("total"));

        var withdrawStats = repository.getReservationStats("WITHDRAWAL");
        response.setWithdrawCount(((Number) withdrawStats.get("count")).longValue());
        response.setWithdrawAmount((BigDecimal) withdrawStats.get("total"));

        response.setTop10RichestWallets(repository.getTop10RichestWallets());

        return response;
    }

    public WalletInformationDto getWalletInformation(Long id) {
        return repository.getWalletInformation(id);
    }

}
