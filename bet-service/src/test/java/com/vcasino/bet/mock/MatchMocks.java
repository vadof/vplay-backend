package com.vcasino.bet.mock;

import com.vcasino.bet.entity.Match;
import com.vcasino.bet.entity.enums.MatchStatus;

import java.time.LocalDateTime;

public class MatchMocks {
    public static Match getMatchMock(Long id) {
        return Match.builder()
                .id(id)
                .tournament(TournamentMocks.getTournamentMocks(1))
                .matchPage("https://some-match-page_" + id + ".com")
                .startDate(LocalDateTime.now().plusDays(1))
                .participant1(PariticpantMocks.getParticipantMock(1, "Vitality"))
                .participant2(PariticpantMocks.getParticipantMock(2, "Spirit"))
                .format("BO3")
                .status(MatchStatus.WAITING_TO_START)
                .winProbability1(0.6)
                .winProbability2(0.4)
                .build();
    }
}
