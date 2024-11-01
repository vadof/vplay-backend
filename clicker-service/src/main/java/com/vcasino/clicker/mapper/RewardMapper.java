package com.vcasino.clicker.mapper;

import com.vcasino.clicker.dto.reward.RewardDto;
import com.vcasino.clicker.entity.Reward;
import com.vcasino.clicker.mapper.common.EntityMapper;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RewardMapper extends EntityMapper<Reward, RewardDto> {
    @Override
    @Mapping(target = "service", source = "integratedService")
    RewardDto toDto(Reward entity);
}
