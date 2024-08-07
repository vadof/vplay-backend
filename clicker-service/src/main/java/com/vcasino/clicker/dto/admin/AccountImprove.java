package com.vcasino.clicker.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountImprove {

    @NotNull(message = "Field cannot be null")
    Long accountId;

    @NotNull(message = "Field cannot be null")
    @Positive(message = "Field should be a positive number")
    Long addCoins;

}
