package com.vcasino.user.dto.email;

import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailTokenOptionsDto {
    @NotEmpty(message = "Field cannot be empty")
    String email;
    @NotEmpty(message = "Field cannot be empty")
    String resendToken;

    Integer emailsSent;
    Boolean canResend;
}
