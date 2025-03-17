package com.vcasino.clicker.controller;

import com.vcasino.clicker.controller.common.GenericController;
import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.dto.CurrencyConversionRequest;
import com.vcasino.clicker.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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

    @Operation(summary = "Convert currency")
    @ApiResponse(responseCode = "200", description = "Currency conversion request is being processed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccountDto.class)))
    @PostMapping(value = "/convert", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> convertCoins(@RequestBody CurrencyConversionRequest conversionRequest) {
        Long accountId = getAccountId();
        log.info("REST request to convert currency Account#{} - {}", accountId, conversionRequest);
        AccountDto account = currencyService.convertCurrency(conversionRequest, accountId);
        return ResponseEntity.ok().body(account);
    }

}
