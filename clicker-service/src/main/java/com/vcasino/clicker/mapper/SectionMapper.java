package com.vcasino.clicker.mapper;

import com.vcasino.clicker.dto.SectionDto;
import com.vcasino.clicker.entity.Section;
import com.vcasino.clicker.mapper.common.EntityMapper;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SectionMapper extends EntityMapper<Section, SectionDto> {

    default Section toSection(String section) {
        if (section != null) {
            Section s = new Section();
            s.setName(section);
            return s;
        }
        return null;
    }

    default String toSection(Section section) {
        if (section != null) {
            return section.getName();
        }
        return null;
    }

}
