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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterParticipantRequest {
    @NotBlank(message = "Name cannot be empty")
    String name;
    String shortName;
    @NotNull(message = "Discipline cannot be null")
    Discipline discipline;
    String participantPage;
}
