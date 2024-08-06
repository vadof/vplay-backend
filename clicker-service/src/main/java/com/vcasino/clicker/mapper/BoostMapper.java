package com.vcasino.clicker.mapper;

import com.vcasino.clicker.dto.BoostDto;
import com.vcasino.clicker.entity.Boost;
import com.vcasino.clicker.mapper.common.EntityMapper;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {ConditionMapper.class}
)
public interface BoostMapper extends EntityMapper<Boost, BoostDto> {
}
