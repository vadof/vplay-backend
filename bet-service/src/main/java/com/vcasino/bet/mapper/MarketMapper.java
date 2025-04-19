package com.vcasino.bet.mapper;

import com.vcasino.bet.dto.response.MarketDto;
import com.vcasino.bet.entity.market.Market;
import com.vcasino.bet.entity.market.handicap.HandicapMaps;
import com.vcasino.bet.entity.market.total.TotalMapRounds;
import com.vcasino.bet.entity.market.winner.WinnerMap;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface MarketMapper {

    @Mapping(target = "outcomeStr", expression = "java(getOutcomeStr(entity, p1Name, p2Name))")
    @Mapping(target = "mapNumber", expression = "java(getMapNumber(entity))")
    MarketDto toDto(Market entity, String p1Name, String p2Name);

    default List<MarketDto> toDtos(List<Market> entityList, String p1Name, String p2Name) {
        if (entityList == null) {
            return new ArrayList<>();
        }
        return entityList.stream()
                .map(entity -> toDto(entity, p1Name, p2Name))
                .toList();
    }

    default String getOutcomeStr(Market entity, String p1Name, String p2Name) {
        if (entity.getType().contains("Winner")) {
            return entity.getOutcome().compareTo(BigDecimal.ONE) == 0 ? p1Name : p2Name;
        } else if (entity.getType().contains("Total")) {
            return entity.getOutcome().compareTo(BigDecimal.ZERO) < 0 ?
                    "Under " + entity.getOutcome().abs() : "Over " + entity.getOutcome();
        } else if (entity instanceof HandicapMaps) {
            String participant = ((HandicapMaps) entity).getParticipant() == 1 ? p1Name : p2Name;
            String outcome = entity.getOutcome().compareTo(BigDecimal.ZERO) < 0 ?
                    entity.getOutcome().toString() : "+" + entity.getOutcome();

            return participant + " " + outcome;
        }
        return null;
    }

    default Integer getMapNumber(Market entity) {
        if (entity instanceof WinnerMap) {
            return ((WinnerMap) entity).getMapNumber();
        } else if (entity instanceof TotalMapRounds) {
            return ((TotalMapRounds) entity).getMapNumber();
        }
        return null;
    }

}
