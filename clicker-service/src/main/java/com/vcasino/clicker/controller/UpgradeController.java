package com.vcasino.clicker.controller;

import com.vcasino.clicker.controller.common.GenericController;
import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.dto.SectionUpgradesDto;
import com.vcasino.clicker.dto.UpgradeUpdateRequest;
import com.vcasino.clicker.service.AccountService;
import com.vcasino.clicker.service.UpgradeService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Upgrade", description = "API operations with Upgrades")
@RestController
@RequestMapping("/api/v1/clicker/upgrades")
@Validated
@Slf4j
public class UpgradeController extends GenericController {

    private final UpgradeService upgradeService;
    private final AccountService accountService;

    public UpgradeController(HttpServletRequest request, UpgradeService upgradeService,
                             AccountService accountService) {
        super(request);
        this.upgradeService = upgradeService;
        this.accountService = accountService;
    }

    @Operation(summary =
            "Get all sections and theirs upgrades. Order field - the order in which the sections should be displayed")
    @ApiResponse(responseCode = "200", description = "Return upgrade list",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
            @Schema(implementation = SectionUpgradesDto[].class)))
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SectionUpgradesDto>> getUpgrades() {
        log.info("REST request to get Upgrades");
        List<SectionUpgradesDto> sectionUpgradesList = upgradeService.getSectionUpgradesList();
        return ResponseEntity.ok().body(sectionUpgradesList);
    }

    @Operation(summary = "Update Upgrade")
    @ApiResponse(responseCode = "200", description = "Successful update",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccountDto.class)))
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> updateUpgrade(@RequestBody @Valid UpgradeUpdateRequest request) {
        log.info("Rest request to buy Upgrade");
        AccountDto updatedAccount = accountService.updateUpgrade(request, getUserId());
        return ResponseEntity.ok().body(updatedAccount);
    }

}
