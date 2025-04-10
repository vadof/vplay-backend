package com.vcasino.bet.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterMatchRequest {
    @NotNull(message = "Tournament cannot be null")
    Integer tournamentId;

    @NotBlank(message = "Match Page cannot be null")
    String matchPage;

    @NotBlank(message = "Participant 1 cannot be null")
    String participant1;

    @NotBlank(message = "Participant 2 cannot be null")
    String participant2;

    @NotBlank(message = "Format cannot be null")
    String format;

    @NotNull(message = "Win probability 1 cannot be null")
    @DecimalMin(value = "0.01", message = "Win probability must be at least 1%")
    @DecimalMax(value = "0.99", message = "Win probability must be max 100%")
    BigDecimal winProbability1;

    @NotNull(message = "Win probability 2 cannot be null")
    @DecimalMin(value = "0.01", message = "Win probability must be at least 1%")
    @DecimalMax(value = "0.99", message = "Win probability must be max 100%")
    BigDecimal winProbability2;

    @NotNull(message = "Start date cannot be null")
    LocalDateTime startDate;
}
