package com.vcasino.user.dto.auth;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OAuthConfirmation {
    @NotEmpty(message = "Field cannot be empty")
    @Size(min = 2, max = 16, message = "Username must be between 2 and 16 characters")
    private String username;
}
