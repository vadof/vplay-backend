package com.vcasino.bet.dto.request;

import com.vcasino.bet.entity.market.MarketResult;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SetMarketResultRequest {
    @NotNull(message = "Field cannot be null")
    List<Long> marketIds;
    @NotNull(message = "Filed cannot be null")
    MarketResult marketResult;
}
