package com.vcasino.clicker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class BuyUpgradeRequest {
    @NotBlank(message = "Field cannot be blank")
    String upgradeName;
    @NotNull(message = "Field cannot be null")
    Integer upgradeLevel;
}
