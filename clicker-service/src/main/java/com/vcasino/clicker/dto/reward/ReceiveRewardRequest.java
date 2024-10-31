package com.vcasino.clicker.dto.reward;

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
public class ReceiveRewardRequest {
    @NotNull(message = "Field cannot be null")
    Integer rewardId;
    @NotNull(message = "Field cannot be null")
    LocalDateTime clickTime;
}
