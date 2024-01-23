package com.casino.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class UserDto {

    private String firstname;
    private String lastname;
    private String username;
    private String email;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal balance;

    @NotNull(message = "Field cannot be null")
    private CurrencyDto currency;

    @NotNull(message = "Field cannot be null")
    private CountryDto country;
}
