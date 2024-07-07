package com.vcasino.user.dto;

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

    private String baseCode;
    private String toBaseCode;
    private BigDecimal amount;
    private BigDecimal result;

}
