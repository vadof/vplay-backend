package com.vcasino.odds.scheduler;

import com.vcasino.odds.service.TrackingService;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class MatchTrackingScheduler {

    private final TrackingService trackingService;

    @Scheduled(cron = "0 0/5 * * * *")
    public void trackUpcomingMatches() {
        trackingService.trackUpcomingMatches();
    }

    @PostConstruct
    public void init() {
        trackingService.trackUpcomingMatches();
    }

}
