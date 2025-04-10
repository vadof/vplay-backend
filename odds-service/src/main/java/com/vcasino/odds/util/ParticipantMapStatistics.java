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
public class ParticipantMapStatistics {
    Integer mapsPlayed1;
    Integer mapsPlayed2;
    Double winRate1;
    Double winRate2;
}
