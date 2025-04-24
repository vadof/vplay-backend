package com.vcasino.bet.controller.admin;

import com.vcasino.bet.dto.request.RegisterMatchRequest;
import com.vcasino.bet.dto.request.RegisterParticipantRequest;
import com.vcasino.bet.dto.request.RegisterTournamentRequest;
import com.vcasino.bet.dto.request.SetMarketResultRequest;
import com.vcasino.bet.entity.Match;
import com.vcasino.bet.entity.Participant;
import com.vcasino.bet.entity.Tournament;
import com.vcasino.bet.entity.enums.Discipline;
import com.vcasino.bet.service.MarketService;
import com.vcasino.bet.service.bet.BetProcessingService;
import com.vcasino.bet.service.image.ImageStorageService;
import com.vcasino.bet.service.MatchService;
import com.vcasino.bet.service.ParticipantService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bet/admin")
@AllArgsConstructor
@Validated
@Slf4j
public class AdminController {

    private final TournamentService tournamentService;
    private final ParticipantService participantService;
    private final MatchService matchService;
    private final ImageStorageService imageStorageService;
    private final MarketService marketService;

    @Operation(summary = "Add new Image")
    @ApiResponse(responseCode = "200", description = "Image added",
            content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE))
    @PostMapping("/images/upload/{folder}")
    public ResponseEntity<List<String>> upload(@PathVariable String folder,
                                         @RequestParam("files") List<MultipartFile> files) {
        log.info("REST request to upload {} images to {}", files.size(), folder);
        List<String> keys = imageStorageService.upload(folder, files);
        return ResponseEntity.ok(keys);
    }

    @Operation(summary = "Get image keys in folder")
    @ApiResponse(responseCode = "200", description = "Return image keys",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping("/images/{folder}")
    public ResponseEntity<List<String>> getImagesInFolder(@PathVariable String folder) {
        log.info("REST request to get images in {}", folder);
        List<String> keys = imageStorageService.getKeysInFolder(folder);
        return ResponseEntity.ok(keys);
    }

    @Operation(summary = "Add new Tournament")
    @ApiResponse(responseCode = "200", description = "Tournament added",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Tournament.class)))
    @PostMapping(value = "/tournaments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Tournament> addTournament(@Valid @RequestBody RegisterTournamentRequest request) {
        log.info("REST request to add Tournament: '{}'", request.getTitle());
        Tournament tournament = tournamentService.addTournament(request);
        return ResponseEntity.ok(tournament);
    }

    @Operation(summary = "Add new Participant")
    @ApiResponse(responseCode = "200", description = "Participant added",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Participant.class)))
    @PostMapping(value = "/participants", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Participant> addParticipant(@Valid @RequestBody RegisterParticipantRequest request) {
        log.info("REST request to add Participant: '{}'", request.getName());
        Participant participant = participantService.addParticipant(request);
        return ResponseEntity.ok(participant);
    }

    @Operation(summary = "Get Participants by discipline")
    @ApiResponse(responseCode = "200", description = "Return participant names",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Participant.class)))
    @GetMapping(value = "/participants", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getParticipants(@RequestParam String discipline) {
        log.info("REST request to get Participants in {} discipline", discipline);
        List<String> res = participantService.getParticipants(Discipline.fromValue(discipline));
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "Add new Match")
    @ApiResponse(responseCode = "200", description = "Match added",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Match.class)))
    @PostMapping(value = "/matches", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Match> addMatch(@Valid @RequestBody RegisterMatchRequest request) {
        log.info("REST request to add Match: {} vs {}", request.getParticipant1(), request.getParticipant2());
        Match match = matchService.addMatch(request);
        return ResponseEntity.ok(match);
    }

    @Operation(summary = "Set result to markets")
    @ApiResponse(responseCode = "200", description = "Return not found ids")
    @PostMapping(value = "/markets/result", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Long>> setMarketResults(@Valid @RequestBody SetMarketResultRequest request) {
        log.info("REST request to set {} result to Markets: {}", request.getMarketResult(), request.getMarketIds());
        List<Long> missingIds = marketService.setResultToMarkets(request);
        return ResponseEntity.ok(missingIds);
    }

}
