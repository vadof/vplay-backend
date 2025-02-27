package com.vcasino.clicker.dto;

import com.vcasino.clicker.entity.enums.ConditionType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConditionDto {
    ConditionType type;
    String upgradeName;
    Integer level;
}
