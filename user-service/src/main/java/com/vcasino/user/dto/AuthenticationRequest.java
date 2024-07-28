package com.vcasino.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {

    @NotEmpty(message = "Field cannot be empty")
    private String username;
    @NotEmpty(message = "Field cannot be empty")
    private String password;

}
