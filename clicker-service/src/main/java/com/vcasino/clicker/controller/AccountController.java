package com.vcasino.clicker.controller;

import com.vcasino.clicker.controller.common.GenericController;
import com.vcasino.clicker.dto.AccountResponse;
import com.vcasino.clicker.dto.LevelDto;
import com.vcasino.clicker.service.AccountService;
import com.vcasino.clicker.service.LevelService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Account", description = "API operations with Clicker Account")
@RestController
@RequestMapping("/api/v1/clicker/accounts")
@Validated
@Slf4j
public class AccountController extends GenericController {

    private final AccountService accountService;
    private final LevelService levelService;

    public AccountController(HttpServletRequest request, AccountService accountService, LevelService levelService) {
        super(request);
        this.accountService = accountService;
        this.levelService = levelService;
    }

    @Operation(summary = "Get account")
    @ApiResponse(responseCode = "200", description = "Return account with upgrades",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccountResponse.class)))
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountResponse> getAccount() {
        log.info("REST request to get Account");
        AccountResponse response = accountService.getAccount(getAccountId());
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Get levels")
    @ApiResponse(responseCode = "200", description = "Return levels",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LevelDto.class)))
    @GetMapping(value = "/levels", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LevelDto>> getLevels() {
        log.info("REST request to get Levels");
        List<LevelDto> levels = levelService.getLevels();
        return ResponseEntity.ok().body(levels);
    }

}
