package com.vcasino.bet.dto.response;

import com.vcasino.bet.entity.market.MarketResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BetDto {
    LocalDateTime date;
    BigDecimal amount;
    BigDecimal odds;
    BigDecimal win;
    String event;
    String outcome;
    MarketResult result;
}
