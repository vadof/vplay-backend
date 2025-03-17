package com.vcasino.common.kafka.event;

import com.vcasino.common.enums.Currency;

import java.math.BigDecimal;

public record CurrencyConversionPayload(Currency from, Currency to, BigDecimal amount) {
}
