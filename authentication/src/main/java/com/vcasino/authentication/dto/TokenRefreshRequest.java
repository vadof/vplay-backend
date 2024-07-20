package com.vcasino.authentication.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenRefreshRequest {
    @NotEmpty(message = "Field cannot be empty")
    private String refreshToken;
}
