package com.vcasino.clicker.dto;

import com.vcasino.common.enums.Currency;
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
public class CurrencyConversionRequest {

    @NotNull(message = "Field cannot be null")
    Currency convertFrom;

    @NotNull(message = "Field cannot be null")
    Currency convertTo;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be a positive number")
    BigDecimal amount;
}
