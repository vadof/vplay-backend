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
public class WalletInformationDto {
    Long id;
    BigDecimal balance;
    BigDecimal reserved;
    Long invitedBy;
    String updatedAt;
    Boolean frozen;
    BigDecimal referralBonus;
    Long totalTransactions;
    List<TransactionDto> latestTransactions;
}
