package com.vcasino.odds.service;

import com.vcasino.odds.entity.Match;
import com.vcasino.odds.entity.enums.Discipline;
import com.vcasino.odds.entity.enums.MatchStatus;
import com.vcasino.odds.repository.MatchRepository;
import com.vcasino.odds.tracker.CsMatchTracker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class TrackingService {

    private final MatchRepository matchRepository;
    private final CsMatchTracker csMatchTracker;

    public void trackUpcomingMatches() {
        int startTrackingBeforeMinutes = 5;
        List<Match> matchesToTrack = matchRepository.findByStartDateBeforeAndStatusNot(
                LocalDateTime.now().plusMinutes(startTrackingBeforeMinutes), MatchStatus.FINISHED);

        for (Match match : matchesToTrack) {
            if (match.getTournament().getDiscipline().equals(Discipline.COUNTER_STRIKE)) {
                csMatchTracker.trackLiveMatchData(match);
            } else {
                log.error("Unknown tournament discipline {}", match.getTournament().getDiscipline());
            }
        }

    }

}
