package com.vcasino.clicker.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "wallet-service")
public interface WalletClient {

    @PostMapping("/internal/currency/conversion")
    ResponseEntity<EventCreatedResponse> convertCurrency(@RequestBody InternalCurrencyConversionRequest request);

}
