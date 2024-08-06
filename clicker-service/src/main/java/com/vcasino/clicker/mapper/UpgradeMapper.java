package com.vcasino.clicker.mapper;

import com.vcasino.clicker.dto.UpgradeDto;
import com.vcasino.clicker.entity.Upgrade;
import com.vcasino.clicker.mapper.common.EntityMapper;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {SectionMapper.class, ConditionMapper.class}
)
public interface UpgradeMapper extends EntityMapper<Upgrade, UpgradeDto> {
}
