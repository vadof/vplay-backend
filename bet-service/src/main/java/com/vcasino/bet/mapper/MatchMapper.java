package com.vcasino.bet.mapper;

import com.vcasino.bet.dto.response.MarketDto;
import com.vcasino.bet.dto.response.MarketPairDto;
import com.vcasino.bet.dto.response.MatchDto;
import com.vcasino.bet.dto.response.MatchMapDto;
import com.vcasino.bet.entity.Match;
import com.vcasino.bet.entity.MatchMap;
import com.vcasino.bet.entity.market.winner.WinnerMatch;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {ParticipantMapper.class, MarketMapper.class}
)
public abstract class MatchMapper implements EntityMapper<Match, MatchDto> {

    @Autowired
    private ParticipantMapper participantMapper;

    @Autowired
    private MarketMapper marketMapper;

    @Override
    @Mapping(target = "startDate", expression = "java(castDateToUTC(entity.getStartDate()))")
    @Mapping(target = "winnerMatchMarkets", expression = "java(getMatchWinnerMarkets(entity))")
    @Mapping(target = "matchMaps", expression = "java(getMatchMaps(entity))")
    public abstract MatchDto toDto(Match entity);

    protected Long castDateToUTC(LocalDateTime date) {
        return date.toEpochSecond(ZoneOffset.UTC);
    }

    protected MarketPairDto getMatchWinnerMarkets(Match entity) {
        String p1Name = entity.getParticipant1().getName();
        String p2Name = entity.getParticipant2().getName();

        List<MarketDto> markets = entity.getMarkets().stream().filter(m -> m instanceof WinnerMatch)
                .sorted(Comparator.comparingInt(m -> m.getOutcome().intValue()))
                .map(m -> marketMapper.toDto(m, p1Name, p2Name)).toList();
        return new MarketPairDto(markets, markets.getFirst().getClosed());
    }

    public List<MatchMapDto> getMatchMaps(Match entity) {
        if (entity.getMatchMaps() == null) return new ArrayList<>();

        List<MatchMapDto> matchMaps = new ArrayList<>();
        for (MatchMap matchMap : entity.getMatchMaps()) {
            int s1 = matchMap.getParticipant1Score();
            int s2 = matchMap.getParticipant2Score();
            if (s1 == 0 && s2 == 0) continue;

            matchMaps.add(new MatchMapDto(matchMap.getMapNumber(), List.of(s1, s2)));
        }

        matchMaps.sort(Comparator.comparingInt(MatchMapDto::getMapNumber));

        return matchMaps;
    }

}
