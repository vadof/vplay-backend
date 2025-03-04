package com.vcasino.clickerdata.utils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SchedulerExecutionDate {
    LocalDateTime lastExecution;
    LocalDateTime now;
}
