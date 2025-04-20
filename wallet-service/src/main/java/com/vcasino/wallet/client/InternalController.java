package com.vcasino.wallet.client;

import com.vcasino.wallet.service.CurrencyConversionService;
import com.vcasino.wallet.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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

    private final CurrencyConversionService currencyConversionService;
    private final CurrencyService currencyService;

    @Operation(summary = """
            Convert currency.
            1. VCoins to VDollars: Amount must be in VCoins.
            2. VDollars to VCoins: Amount must be in VDollars.""")
    @ApiResponse(responseCode = "200", description = "Pending event created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EventCreatedResponse.class)))
    @PostMapping(value = "/currency/conversion")
    public ResponseEntity<EventCreatedResponse> convertCurrency(@RequestBody InternalCurrencyConversionRequest request) {
        log.info("Request to convert currency {}", request);
        EventCreatedResponse response = currencyConversionService.convertCurrency(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create VDollars transaction event")
    @ApiResponse(responseCode = "200", description = "Pending event created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EventCreatedResponse.class)))
    @PostMapping(value = "/currency/reserve")
    public ResponseEntity<EventCreatedResponse> reserveCurrency(@RequestBody ReservationRequest request) {
        log.info("Request to reserve currency {}", request);
        EventCreatedResponse response = currencyService.reserveCurrency(request);
        return ResponseEntity.ok(response);
    }

}
