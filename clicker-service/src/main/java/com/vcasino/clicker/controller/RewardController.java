package com.vcasino.clicker.controller;

import com.vcasino.clicker.controller.common.GenericController;
import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.dto.reward.ReceiveRewardRequest;
import com.vcasino.clicker.dto.reward.RewardDto;
import com.vcasino.clicker.dto.streak.StreakInfo;
import com.vcasino.clicker.service.RewardService;
import com.vcasino.clicker.service.StreakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

@RestController
@RequestMapping("/rewards")
@Validated
@Slf4j
public class RewardController extends GenericController {

    private final StreakService streakService;
    private final RewardService rewardService;

    public RewardController(HttpServletRequest request, StreakService streakService, RewardService rewardService) {
        super(request);
        this.streakService = streakService;
        this.rewardService = rewardService;
    }

    @Operation(summary = "Get rewards info")
    @ApiResponse(responseCode = "200", description = "Return rewards info",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
            @Schema(implementation = RewardDto[].class)))
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RewardDto>> getRewardsInfo() {
        log.info("Rest request to get rewards info");
        List<RewardDto> rewards = rewardService.getRewards(getAccountId());
        return ResponseEntity.ok().body(rewards);
    }

    @Operation(summary = "Receive reward")
    @ApiResponse(responseCode = "200", description = "Reward received",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
            @Schema(implementation = AccountDto.class)))
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> receiveReward(@RequestBody @Valid ReceiveRewardRequest rewardRequest) {
        log.info("Rest request to receive reward");
        AccountDto accountDto = rewardService.receiveReward(getAccountId(), rewardRequest);
        return ResponseEntity.ok().body(accountDto);
    }

    @Operation(summary = "Get streak info")
    @ApiResponse(responseCode = "200", description = "Return streaks",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
            @Schema(implementation = StreakInfo.class)))
    @GetMapping(value = "/streaks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreakInfo> getStreakInfo() {
        log.info("Rest request to get StreakInfo");
        StreakInfo streakInfo = streakService.getStreakInfo(getAccountId());
        return ResponseEntity.ok().body(streakInfo);
    }

    @Operation(summary = "Receive streak reward")
    @ApiResponse(responseCode = "200", description = "Reward received",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
            @Schema(implementation = AccountDto.class)))
    @PostMapping(value = "/streaks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> receiveStreakReward() {
        log.info("Rest request to receive streak reward");
        AccountDto accountDto = streakService.receiveReward(getAccountId());
        return ResponseEntity.ok().body(accountDto);
    }

}
