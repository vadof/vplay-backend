package com.vcasino.wallet.controller;

import com.vcasino.wallet.dto.BalanceDto;
import com.vcasino.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Wallet", description = "API operations with Wallet")
@RestController
@RequestMapping("/api/v1/wallet")
@Slf4j
public class WalletController extends GenericController {

    private final WalletService walletService;

    public WalletController(HttpServletRequest request, WalletService walletService) {
        super(request);
        this.walletService = walletService;
    }

    @Operation(summary = "Get wallet balance")
    @ApiResponse(responseCode = "200", description = "Return wallet balance",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BalanceDto.class)))
    @GetMapping(value = "/balance", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BalanceDto> getBalance() {
        log.info("REST request to get Balance");
        BalanceDto balance = walletService.getBalance(getLoggedInUserId());
        return ResponseEntity.ok(balance);
    }

}
