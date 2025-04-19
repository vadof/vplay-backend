package com.vcasino.bet.mapper;

import com.vcasino.bet.dto.response.TournamentDto;
import com.vcasino.bet.entity.Tournament;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TournamentMapper extends EntityMapper<Tournament, TournamentDto> {
}
