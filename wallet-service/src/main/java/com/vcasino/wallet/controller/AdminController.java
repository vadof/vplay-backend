package com.vcasino.wallet.controller;

import com.vcasino.wallet.dto.BalanceDto;
import com.vcasino.wallet.dto.DepositRequestDto;
import com.vcasino.wallet.dto.ReferralBonusDto;
import com.vcasino.wallet.dto.statistics.ServiceStatisticsDto;
import com.vcasino.wallet.dto.statistics.WalletInformationDto;
import com.vcasino.wallet.service.ReferralBonusService;
import com.vcasino.wallet.service.StatisticsService;
import com.vcasino.wallet.service.WalletService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin", description = "Admin functionality")
@RestController
@RequestMapping("/api/v1/wallet/admin")
@Validated
@Slf4j
public class AdminController extends GenericController {

    private final StatisticsService statisticsService;
    private final WalletService walletService;
    private final ReferralBonusService referralBonusService;

    public AdminController(HttpServletRequest request, WalletService walletService, ReferralBonusService referralBonusService,
                           StatisticsService statisticsService) {
        super(request);
        this.walletService = walletService;
        this.referralBonusService = referralBonusService;
        this.statisticsService = statisticsService;
    }

    @Operation(summary = "Get statistics")
    @ApiResponse(responseCode = "200", description = "Return statistics",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ServiceStatisticsDto.class)))
    @GetMapping(value = "/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServiceStatisticsDto> getStatistics() {
        validateAdminRole();
        log.info("REST request to get Service statistics");
        ServiceStatisticsDto statistics = statisticsService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Get Wallet Information")
    @ApiResponse(responseCode = "200", description = "Return Wallet Information",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = WalletInformationDto.class)))
    @GetMapping(value = "/statistics/wallet", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WalletInformationDto> getStatistics(@RequestParam(name = "id") Long id) {
        validateAdminRole();
        log.info("REST request to get Wallet#{} information", id);
        WalletInformationDto information = statisticsService.getWalletInformation(id);
        return ResponseEntity.ok(information);
    }

    @Operation(summary = "Deposit to wallet")
    @ApiResponse(responseCode = "200", description = "Return updated balance",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BalanceDto.class)))
    @PostMapping(value = "/deposit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BalanceDto> depositToWallet(@Valid @RequestBody DepositRequestDto request) {
        validateAdminRole();
        log.info("REST request to Deposit {} VDollars to Wallet#{}", request.getAmount(), request.getWalletId());
        BalanceDto updatedBalance = walletService.depositToWallet(request, getLoggedInUserId());
        return ResponseEntity.ok(updatedBalance);
    }

    @Operation(summary = "Add referral bonus")
    @ApiResponse(responseCode = "200", description = "Referral bonus added")
    @PostMapping(value = "/referral", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addReferralBonus(@Valid @RequestBody ReferralBonusDto referralBonusDto) {
        validateAdminRole();
        log.info("REST request to add referral bonus for Wallet#{}", referralBonusDto.getReferralId());
        referralBonusService.addReferralBonus(referralBonusDto);
        return ResponseEntity.ok(null);
    }

}
