package com.vcasino.wallet;

import com.vcasino.common.kafka.event.CurrencyConversionEvent;
import com.vcasino.wallet.service.WalletService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallet")
@AllArgsConstructor
@Slf4j
public class Controll {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<Void> send(@RequestBody CurrencyConversionEvent event) {
        log.info("Received currency conversion event - " + event);
        walletService.convertCurrency(event);
        return ResponseEntity.ok().body(null);
    }

}
