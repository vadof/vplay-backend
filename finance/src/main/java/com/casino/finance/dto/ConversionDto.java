package com.casino.finance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversionDto {

    @NotEmpty(message = "Base Code cannot be empty")
    private String baseCode;

    @NotEmpty(message = "To Base Code cannot be empty")
    private String toBaseCode;

    @NotNull
    @Min(value = 1, message = "Amount should be greater or equal to 1")
    private BigDecimal amount;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal result;

    public ConversionDto copy() {
        return ConversionDto.builder()
                .baseCode(baseCode.toUpperCase())
                .toBaseCode(toBaseCode.toUpperCase())
                .amount(amount)
                .result(result)
                .build();
    }
}
