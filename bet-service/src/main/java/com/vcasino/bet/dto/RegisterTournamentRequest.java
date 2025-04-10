package com.vcasino.bet.dto;

import com.vcasino.bet.entity.enums.Discipline;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterTournamentRequest {

    @NotBlank(message = "Title cannot be empty")
    String title;

    @NotNull(message = "Discipline cannot be empty")
    Discipline discipline;

    String tournamentPage;

    @NotNull(message = "Start date cannot be null")
    LocalDateTime startDate;

    @NotNull(message = "Start date cannot be null")
    LocalDateTime endDate;

}
