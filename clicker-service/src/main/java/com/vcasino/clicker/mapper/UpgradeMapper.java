package com.vcasino.clicker.mapper;

import com.vcasino.clicker.dto.UpgradeDto;
import com.vcasino.clicker.entity.Section;
import com.vcasino.clicker.entity.Upgrade;
import com.vcasino.clicker.mapper.common.EntityMapper;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {ConditionMapper.class}
)
public interface UpgradeMapper extends EntityMapper<Upgrade, UpgradeDto> {

    @Override
    @Mapping(target = "section", source = "section.name")
    UpgradeDto toDto(Upgrade entity);

    @Override
    @Mapping(target = "section", expression = "java(mapSection(dto.getSection()))")
    Upgrade toEntity(UpgradeDto dto);

    default Section mapSection(String sectionName) {
        if (sectionName == null) {
            return null;
        }
        Section section = new Section();
        section.setName(sectionName);
        return section;
    }

}
