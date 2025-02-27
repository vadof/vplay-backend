package com.vcasino.clicker.controller;

import com.vcasino.clicker.controller.common.GenericController;
import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.dto.Tap;
import com.vcasino.clicker.service.ClickerService;
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

@Tag(name = "Clicker", description = "API operations with Clicker")
@RestController
@RequestMapping("/api/v1/clicker")
@Validated
@Slf4j
public class ClickerController extends GenericController {

    private final ClickerService clickerService;

    public ClickerController(HttpServletRequest request, ClickerService clickerService) {
        super(request);
        this.clickerService = clickerService;
    }

    @Operation(summary = "Send info about taps")
    @ApiResponse(responseCode = "200", description = "Return updated account",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccountDto.class)))
    @PostMapping(value = "/tap", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> tap(@Valid @RequestBody Tap tap) {
        Long accountId = getAccountId();
        log.info("REST request to Tap: {}, Account#{}", tap.getAmount(), accountId);
        AccountDto account = clickerService.tap(tap, accountId);
        return ResponseEntity.ok().body(account);
    }

}
