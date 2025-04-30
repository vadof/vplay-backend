package com.vcasino.bet.controller;

import com.vcasino.bet.controller.common.GenericController;
import com.vcasino.bet.dto.request.BetRequest;
import com.vcasino.bet.dto.response.BetDto;
import com.vcasino.bet.dto.response.BetResponse;
import com.vcasino.bet.dto.response.PaginatedResponse;
import com.vcasino.bet.service.bet.BetService;
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

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/bet")
@Validated
@Slf4j
public class BetController extends GenericController {

    private final BetService betService;

    public BetController(HttpServletRequest request, BetService betService) {
        super(request);
        this.betService = betService;
    }

    @PostMapping(value = "/place", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<BetResponse>> placeBet(@Valid @RequestBody BetRequest request) {
        log.info("REST request to place bet {}", request);
        return betService.addBetToProcessing(request, getUserId())
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedResponse<BetDto>> getBets(@RequestParam(defaultValue = "0") Integer page) {
        log.debug("REST request to get bets");
        PaginatedResponse<BetDto> res = betService.getUserBets(getUserId(), page);
        return ResponseEntity.ok(res);
    }

}
