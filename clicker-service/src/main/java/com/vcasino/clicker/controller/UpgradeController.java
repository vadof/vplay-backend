package com.vcasino.clicker.controller;

import com.vcasino.clicker.controller.common.GenericController;
import com.vcasino.clicker.dto.AccountResponse;
import com.vcasino.clicker.dto.BuyUpgradeRequest;
import com.vcasino.clicker.service.AccountService;
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

@Tag(name = "Upgrade", description = "API operations with Upgrades")
@RestController
@RequestMapping("/api/v1/clicker/upgrades")
@Validated
@Slf4j
public class UpgradeController extends GenericController {

    private final AccountService accountService;

    public UpgradeController(HttpServletRequest request, AccountService accountService) {
        super(request);
        this.accountService = accountService;
    }

    @Operation(summary = "Buy Upgrade")
    @ApiResponse(responseCode = "200", description = "Successful purchase. Return account with upgrades",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccountResponse.class)))
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountResponse> buyUpgrade(@RequestBody @Valid BuyUpgradeRequest request) {
        log.debug("Rest request to buy Upgrade: {}", request.getUpgradeName());
        AccountResponse updatedAccount = accountService.buyUpgrade(request, getAccountId());
        return ResponseEntity.ok().body(updatedAccount);
    }

}
