package com.vcasino.bet.dto;

import com.vcasino.bet.entity.enums.Discipline;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RedisTournamentDto {
    Integer id;
    String image;
    String title;
    Discipline discipline;
    List<Long> matchKeys;
}
