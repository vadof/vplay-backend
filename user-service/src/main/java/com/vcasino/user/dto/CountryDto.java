package com.vcasino.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class CountryDto {
    private String code;
    private String name;
}
