package com.vcasino.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DepositRequestDto {
    @NotNull(message = "Field cannot be null")
    Long walletId;

    @NotNull(message = "Field cannot be null")
    @DecimalMin(value = "0.01", message = "Min amount is 0.01")
    BigDecimal amount;
}
