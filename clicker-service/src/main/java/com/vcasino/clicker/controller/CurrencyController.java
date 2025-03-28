package com.vcasino.clicker.controller;

import com.vcasino.clicker.controller.common.GenericController;
import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.dto.AccountWalletResponse;
import com.vcasino.clicker.dto.CurrencyConversionRequest;
import com.vcasino.clicker.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Currency", description = "API operations with Currency")
@RestController
@RequestMapping("/api/v1/clicker/currency")
@Validated
@Slf4j
public class CurrencyController extends GenericController {

    private final CurrencyService currencyService;

    public CurrencyController(HttpServletRequest request, CurrencyService currencyService) {
        super(request);
        this.currencyService = currencyService;
    }

    @Operation(summary = "Convert VCoins to VDollars")
    @ApiResponse(responseCode = "200", description = "Currency conversion request is being processed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccountDto.class)))
    @PostMapping(value = "/vcoins/vdollars", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> convertToVDollars(@Valid @RequestBody CurrencyConversionRequest conversionRequest) {
        Long accountId = getAccountId();
        log.info("REST request to convert to VDollars Account#{}, Amount: {}", accountId, conversionRequest.getAmount());
        AccountDto account = currencyService.convertToVDollars(conversionRequest, accountId);
        return ResponseEntity.ok().body(account);
    }

    @Operation(summary = "Convert VDollars to VCoins")
    @ApiResponse(responseCode = "200", description = "Currency conversion request is being processed." +
            " Return account with updated wallet balance",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccountWalletResponse.class)))
    @PostMapping(value = "/vdollars/vcoins", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountWalletResponse> convertToVCoins(@Valid @RequestBody CurrencyConversionRequest conversionRequest) {
        Long accountId = getAccountId();
        log.info("REST request to convert to VCoins Account#{}, Amount: {}", accountId, conversionRequest.getAmount());
        AccountWalletResponse response = currencyService.convertToVCoins(conversionRequest, accountId);
        return ResponseEntity.ok().body(response);
    }

}
