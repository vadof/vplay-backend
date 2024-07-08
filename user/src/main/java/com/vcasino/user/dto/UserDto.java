package com.vcasino.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    @NotNull(message = "Field cannot be null")
    private String firstname;

    @NotNull(message = "Field cannot be null")
    private String lastname;

    @NotNull(message = "Field cannot be null")
    private String username;

    @NotNull(message = "Field cannot be null")
    private String email;

    @NotNull(message = "Field cannot be null")
    private CountryDto country;
}
