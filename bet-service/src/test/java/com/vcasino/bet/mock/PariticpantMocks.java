package com.vcasino.bet.mock;

import com.vcasino.bet.entity.Participant;
import com.vcasino.bet.entity.enums.Discipline;

public class PariticpantMocks {
    public static Participant getParticipantMock(Integer id, String name) {
        return Participant.builder()
                .id(id)
                .name(name)
                .shortName(name)
                .discipline(Discipline.COUNTER_STRIKE)
                .image("participants/" + name + ".webp")
                .participantPage("https://some-participant-page_" + name + ".com")
                .build();
    }
}
