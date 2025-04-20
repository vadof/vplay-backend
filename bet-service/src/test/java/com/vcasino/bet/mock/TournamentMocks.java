package com.vcasino.bet.mock;

import com.vcasino.bet.entity.Tournament;
import com.vcasino.bet.entity.enums.Discipline;

import java.time.LocalDateTime;

public class TournamentMocks {
    public static Tournament getTournamentMocks(Integer id) {
        return Tournament.builder()
                .id(id)
                .title("Blast Austin Major 2025")
                .discipline(Discipline.COUNTER_STRIKE)
                .tournamentPage("https://tournament-page.com")
                .image("tournament/blast-austin-major-2025.webp")
                .startDate(LocalDateTime.now().minusDays(3))
                .endDate(LocalDateTime.now().plusDays(3))
                .build();
    }
}
