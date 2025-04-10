package com.vcasino.bet.controller;

import com.vcasino.bet.dto.RegisterParticipantRequest;
import com.vcasino.bet.entity.Participant;
import com.vcasino.bet.service.ParticipantService;
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
@RequestMapping("/api/v1/bet/admin/participants")
@AllArgsConstructor
@Validated
@Slf4j
public class ParticipantController {

    private final ParticipantService participantService;

    @Operation(summary = "Add new Participant")
    @ApiResponse(responseCode = "200", description = "Participant added",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Participant.class)))
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Participant> addParticipant(@Valid @RequestBody RegisterParticipantRequest request) {
        log.info("REST request to add Participant: '{}'", request.getName());
        Participant participant = participantService.addParticipant(request);
        return ResponseEntity.ok(participant);
    }

}
