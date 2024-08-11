package com.vcasino.clicker.controller;

import com.vcasino.clicker.dto.SectionUpgradesDto;
import com.vcasino.clicker.service.UpgradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Upgrade", description = "API operations with Upgrades")
@RestController
@RequestMapping("/api/v1/clicker/upgrades")
@AllArgsConstructor
@Slf4j
public class UpgradeController {

    private final UpgradeService upgradeService;

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

}
