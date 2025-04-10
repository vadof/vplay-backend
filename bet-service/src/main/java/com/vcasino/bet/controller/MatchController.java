package com.vcasino.bet.controller;

import com.vcasino.bet.dto.RegisterMatchRequest;
import com.vcasino.bet.entity.Match;
import com.vcasino.bet.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

@RestController
@RequestMapping("/api/v1/bet/admin/matches")
@AllArgsConstructor
@Validated
@Slf4j
public class MatchController {

    private final MatchService matchService;

    @Operation(summary = "Add new Match")
    @ApiResponse(responseCode = "200", description = "Match added",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Match.class)))
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Match> addMatch(@Valid @RequestBody RegisterMatchRequest request) {
        log.info("REST request to add Match: {} vs {}", request.getParticipant1(), request.getParticipant2());
        Match match = matchService.addMatch(request);
        return ResponseEntity.ok(match);
    }

}
