package com.vcasino.clicker.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "wallet-service")
public interface WalletClient {

    @PostMapping("/internal/currency/conversion")
    @CircuitBreaker(name = "wallet")
    ResponseEntity<EventCreatedResponse> convertCurrency(@RequestBody InternalCurrencyConversionRequest request);

}
