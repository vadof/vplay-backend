package com.vcasino.user.dto.email;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailConfirmation {
    @NotEmpty(message = "Field cannot be empty")
    private String confirmationToken;
}
