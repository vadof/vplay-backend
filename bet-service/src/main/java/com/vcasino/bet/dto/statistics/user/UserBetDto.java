package com.vcasino.bet.dto.statistics.user;

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
public class UserBetDto {
    BigDecimal betOdds;
    BigDecimal betAmount;
    String createdAt;
    String betResult;
    String marketOutcome;
    String matchDescription;
}
