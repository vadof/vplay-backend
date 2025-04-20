package com.vcasino.bet.dto.request;

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
public class BetRequest {
    @NotNull(message = "Market id cannot be null")
    Long marketId;

    @NotNull(message = "Odds cannot be null")
    BigDecimal odds;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.1", message = "Amount must be at least 0.1")
    BigDecimal amount;

    @NotNull(message = "Field cannot null")
    Boolean acceptAllOddsChanges;
}
