package com.vcasino.bet.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "odds-service")
public interface OddsClient {

    @PostMapping("/internal/market/initialization")
    ResponseEntity<Void> initializeMarkets(@RequestBody MarketInitializationRequest request);

}
