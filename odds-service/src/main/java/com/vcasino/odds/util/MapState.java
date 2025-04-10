package com.vcasino.odds.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MapState {
    String mapName;
    Integer ctScore;
    Integer tScore;
    Boolean isTeam1CT;
    RoundState roundState;
}
