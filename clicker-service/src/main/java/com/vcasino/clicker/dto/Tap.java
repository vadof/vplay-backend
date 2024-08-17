package com.vcasino.clicker.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Tap {

    @NotNull(message = "Field cannot be null")
    @Positive(message = "Field should be a positive number")
    Integer amount;

    @NotNull(message = "Field cannot be null")
    @PositiveOrZero(message = "Field should be a positive number or zero")
    Integer availableTaps;

    @NotNull(message = "Field cannot be null")
    Long timestamp;

}
