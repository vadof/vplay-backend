package com.vcasino.bet.mapper;

import com.vcasino.bet.dto.response.BetDto;
import com.vcasino.bet.entity.Bet;
import com.vcasino.bet.entity.Match;
import com.vcasino.bet.entity.Tournament;
import com.vcasino.bet.entity.market.Market;
import com.vcasino.bet.entity.market.MarketResult;
import com.vcasino.bet.entity.market.winner.WinnerMap;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {MarketMapper.class}
)
public abstract class BetMapper implements EntityMapper<Bet, BetDto> {

    @Autowired
    private MarketMapper marketMapper;

    @Override
    @Mapping(target = "date", source = "createdAt")
    @Mapping(target = "win", expression = "java(getWin(entity))")
    @Mapping(target = "event", expression = "java(getEvent(entity))")
    @Mapping(target = "outcome", expression = "java(getOutcome(entity))")
    public abstract BetDto toDto(Bet entity);

    protected BigDecimal getWin(Bet entity) {
        return MarketResult.WIN.equals(entity.getResult())
                ? entity.getAmount().multiply(entity.getOdds()).setScale(2, RoundingMode.DOWN)
                : null;
    }

    protected String getEvent(Bet entity) {
        Match match = entity.getMarket().getMatch();
        Tournament tournament = match.getTournament();

        return "%s. %s. %s vs %s".formatted(tournament.getDiscipline().getName(), tournament.getTitle(),
                match.getParticipant1().getName(), match.getParticipant2().getName());
    }

    protected String getOutcome(Bet entity) {
        Market market = entity.getMarket();
        Match match = entity.getMarket().getMatch();

        String marketTypeStr = "";
        String outcomeName = marketMapper.getOutcomeStr(market,
                match.getParticipant1().getName(), match.getParticipant2().getName());

        marketTypeStr = switch (market.getType()) {
            case "WinnerMatch" -> "Match Winner";
            case "WinnerMap" -> "Winner. Map " + ((WinnerMap) market).getMapNumber();
            case "TotalMaps" -> "Total Maps";
            case "TotalMapRounds" -> "Total";
            case "HandicapMaps" -> "Handicap Maps";
            default -> marketTypeStr;
        };

        return marketTypeStr + ". " + outcomeName;
    }

}
