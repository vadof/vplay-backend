package com.vcasino.wallet.dto.statistics;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceStatisticsDto {
    BigDecimal totalWalletsBalance;
    Long totalTransactions;
    BigDecimal vDollarToVCoinAmount;
    BigDecimal vCoinToVDollarAmount;
    Long depositCount;
    BigDecimal depositAmount;
    Long withdrawCount;
    BigDecimal withdrawAmount;
    List<TopWalletDto> top10RichestWallets;
}
