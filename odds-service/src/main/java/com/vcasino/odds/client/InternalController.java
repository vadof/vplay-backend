package com.vcasino.odds.client;

import com.vcasino.odds.service.MarketService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/internal")
@AllArgsConstructor
@Slf4j
public class InternalController {

    private final MarketService marketService;

    @PostMapping(value = "/market/initialization")
    public ResponseEntity<Void> initializeMarkets(@RequestBody MarketInitializationRequest request) {
        log.info("Request to initialize markets for Match#{}", request.getMatchId());
        marketService.initializeMarkets(request);
        return ResponseEntity.ok(null);
    }

}
