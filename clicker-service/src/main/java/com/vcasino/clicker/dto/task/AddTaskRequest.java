package com.vcasino.clicker.dto.task;

import com.vcasino.clicker.config.IntegratedService;
import com.vcasino.clicker.dto.DateRange;
import com.vcasino.clicker.entity.enums.TaskType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;

@Validated
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddTaskRequest {
    @NotEmpty(message = "field cannot be empty")
    String id;

    @NotNull(message = "field cannot be null")
    TaskType taskType;

    @NotNull(message = "field cannot be null")
    IntegratedService service;

    @NotNull(message = "field cannot be empty")
    @Positive(message = "must be positive")
    Integer rewardCoins;

    @NotEmpty(message = "field cannot be empty")
    String taskName;

    @Valid
    DateRange dateRange;
}
