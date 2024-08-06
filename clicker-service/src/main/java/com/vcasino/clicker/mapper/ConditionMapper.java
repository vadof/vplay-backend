package com.vcasino.clicker.mapper;

import com.vcasino.clicker.dto.ConditionDto;
import com.vcasino.clicker.entity.Condition;
import com.vcasino.clicker.mapper.common.EntityMapper;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ConditionMapper extends EntityMapper<Condition, ConditionDto> {
}
