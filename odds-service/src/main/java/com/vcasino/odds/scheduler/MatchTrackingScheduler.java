package com.vcasino.odds.scheduler;

import com.vcasino.odds.client.MarketInitializationRequest;
import com.vcasino.odds.entity.Match;
import com.vcasino.odds.repository.MarketRepository;
import com.vcasino.odds.repository.MatchRepository;
import com.vcasino.odds.service.MarketService;
import com.vcasino.odds.service.TrackingService;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class MatchTrackingScheduler {

    private final TrackingService trackingService;
    private final MarketService marketService;
    private final MatchRepository matchRepository;

    // TODO uncomment
//    @Scheduled(cron = "0 0/15 * * * *")
//    @Scheduled(fixedDelay = 60000)
//    public void trackUpcomingMatches() {
//        log.info("Update match trackers");
//        trackingService.trackUpcomingMatches();
//    }

    // TODO remove
    @PostConstruct
    public void initMarkets() throws InterruptedException {
        List<Match> matches = matchRepository.findAll();
        for (Match match : matches) {
            if (match.getMarkets() == null || match.getMarkets().isEmpty()) {
                marketService.initializeMarkets(new MarketInitializationRequest(match.getId()));
            }
        }

        Thread.sleep(1000);

        trackingService.trackUpcomingMatches();
    }

}
