package com.vcasino.odds.tracker;

import com.vcasino.odds.entity.Match;
import com.vcasino.odds.entity.MatchMap;
import com.vcasino.odds.parser.CsMatchParser;
import com.vcasino.odds.util.MapState;
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
public class MatchData {
    CsMatchParser matchParser;
    Match match;
    MatchMap currentMap;
    MapState mapState;
}
