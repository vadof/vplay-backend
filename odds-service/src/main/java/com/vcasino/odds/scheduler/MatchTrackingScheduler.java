package com.vcasino.odds.scheduler;

import com.vcasino.odds.service.TrackingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class MatchTrackingScheduler {

    private final TrackingService trackingService;

    // TODO uncomment
//    @Scheduled(cron = "0 0/15 * * * *")
//    @Scheduled(fixedDelay = 10000)
//    public void trackUpcomingMatches() {
//        log.info("Update match trackers");
//        trackingService.trackUpcomingMatches();
//    }

}
