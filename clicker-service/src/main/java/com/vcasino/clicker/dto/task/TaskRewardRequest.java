package com.vcasino.clicker.dto.task;

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
public class TaskRewardRequest {
    @NotNull(message = "Field cannot be null")
    Integer taskId;
    @NotNull(message = "Field cannot be null")
    LocalDateTime clickTime;
}
