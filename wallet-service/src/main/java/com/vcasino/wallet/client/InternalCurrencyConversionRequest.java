package com.vcasino.wallet.client;

import com.vcasino.common.enums.Currency;
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
public class InternalCurrencyConversionRequest {
    Currency from;
    Currency to;
    BigDecimal amount;
    Long accountId;
}
