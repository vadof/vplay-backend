package com.vcasino.odds.service;

import com.vcasino.odds.entity.Match;
import com.vcasino.odds.entity.enums.Discipline;
import com.vcasino.odds.repository.MatchRepository;
import com.vcasino.odds.tracker.CsMatchTracker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class TrackingService {

    private final MatchRepository matchRepository;
    private final CsMatchTracker csMatchTracker;

    @Async
    public void trackUpcomingMatches() {
        // TODO uncomment
        int startTrackingBeforeMinutes = 30;
//        List<Match> matchesToTrack = matchRepository.findByStartDateBeforeAndStatus(
//                LocalDateTime.now().plusMinutes(startTrackingBeforeMinutes), MatchStatus.WAITING_TO_START);
        List<Match> matchesToTrack = matchRepository.findAll();

        for (Match match : matchesToTrack) {
            if (match.getTournament().getDiscipline().equals(Discipline.COUNTER_STRIKE)) {
                csMatchTracker.trackLiveMatchData(match);
            } else {
                log.error("Unknown tournament discipline {}", match.getTournament().getDiscipline());
            }
        }

    }

}
