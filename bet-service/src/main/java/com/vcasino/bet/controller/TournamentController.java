package com.vcasino.bet.controller;

import com.vcasino.bet.dto.RegisterTournamentRequest;
import com.vcasino.bet.entity.Tournament;
import com.vcasino.bet.service.TournamentService;
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
@RequestMapping("/api/v1/bet/admin/tournaments")
@AllArgsConstructor
@Validated
@Slf4j
public class TournamentController {

    private final TournamentService tournamentService;

    @Operation(summary = "Add new Tournament")
    @ApiResponse(responseCode = "200", description = "Tournament added",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Tournament.class)))
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Tournament> addTournament(@Valid @RequestBody RegisterTournamentRequest request) {
        log.info("REST request to add Tournament: '{}'", request.getTitle());
        Tournament tournament = tournamentService.addTournament(request);
        return ResponseEntity.ok(tournament);
    }

}
