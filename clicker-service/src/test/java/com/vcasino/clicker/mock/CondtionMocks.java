package com.vcasino.clicker.mock;

import com.vcasino.clicker.dto.ConditionDto;
import com.vcasino.clicker.entity.Condition;
import com.vcasino.clicker.entity.ConditionType;

public class CondtionMocks {

    public static Condition getConditionMock(String upgradeName, Integer level) {
        return Condition.builder()
                .id(1)
                .type(ConditionType.BY_UPGRADE)
                .level(level)
                .upgradeName(upgradeName)
                .build();
    }

    public static ConditionDto getConditionDtoMock(String upgradeName, Integer level) {
        return ConditionDto.builder()
                .type(ConditionType.BY_UPGRADE)
                .level(level)
                .upgradeName(upgradeName)
                .build();
    }

}
