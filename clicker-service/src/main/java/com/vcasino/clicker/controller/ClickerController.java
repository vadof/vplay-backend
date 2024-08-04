package com.vcasino.clicker.controller;

import com.vcasino.clicker.dto.Tap;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.service.ClickerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "API operations with Clicker")
@RestController
@RequestMapping("/api/v1/clicker")
@AllArgsConstructor
@Validated
@Slf4j
public class ClickerController {

    private final ClickerService clickerService;

    @Operation(summary = "Send info about taps")
    @ApiResponse(responseCode = "200", description = "Return updated account",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Account.class)))
    @PostMapping(value = "/tap", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Account> tap(@Valid @RequestBody Tap tap) {
        log.info("REST request to tap");
        Account account = clickerService.tap(tap);
        return ResponseEntity.ok().body(account);
    }

}
