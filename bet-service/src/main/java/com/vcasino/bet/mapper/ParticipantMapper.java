package com.vcasino.bet.mapper;

import com.vcasino.bet.dto.response.ParticipantDto;
import com.vcasino.bet.entity.Participant;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ParticipantMapper extends EntityMapper<Participant, ParticipantDto> {
}
