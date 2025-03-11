package com.vcasino.clicker.dto.task;

import com.vcasino.clicker.config.IntegratedService;
import com.vcasino.clicker.entity.enums.TaskType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SupportedTaskServices {
    TaskType taskType;
    List<IntegratedService> services;
}
