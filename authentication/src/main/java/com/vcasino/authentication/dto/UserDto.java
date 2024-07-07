package com.vcasino.authentication.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
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

    @NotEmpty(message = "Field cannot be empty")
    private String firstname;

    @NotEmpty(message = "Field cannot be empty")
    private String lastname;

    @NotEmpty(message = "Field cannot be empty")
    private String username;

    @NotEmpty(message = "Field cannot be empty")
    @Email(message = "Invalid email")
    private String email;

    @Size(min = 8, message = "Min password length is 8")
    @NotEmpty(message = "Field cannot be empty")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal balance;

    @NotNull(message = "Field cannot be null")
    private CurrencyDto currency;

    @NotNull(message = "Field cannot be null")
    private CountryDto country;
}
