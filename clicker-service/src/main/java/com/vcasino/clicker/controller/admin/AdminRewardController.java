package com.vcasino.clicker.controller.admin;

import com.vcasino.clicker.config.IntegratedService;
import com.vcasino.clicker.controller.common.GenericController;
import com.vcasino.clicker.dto.reward.AddRewardRequest;
import com.vcasino.clicker.dto.youtube.VideoInfo;
import com.vcasino.clicker.entity.enums.RewardType;
import com.vcasino.clicker.service.RewardService;
import io.swagger.v3.oas.annotations.Hidden;
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

import java.util.List;
import java.util.Map;

@Hidden
@RestController
@RequestMapping("/api/v1/clicker/admin/rewards")
@Validated
@Slf4j
public class AdminRewardController extends GenericController {

    private final RewardService rewardService;

    public AdminRewardController(HttpServletRequest request, RewardService rewardService) {
        super(request);
        this.rewardService = rewardService;
    }

    @GetMapping(value = "/properties", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<RewardType, List<IntegratedService>>> getSupportedServicesByRewardType() {
        log.info("REST request to get supported services by reward type");
        validateAdminRole();
        return ResponseEntity.ok().body(rewardService.getSupportedServicesByRewardType());
    }

    @GetMapping(value = "/video-info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VideoInfo> getVideoInfo(@RequestParam String videoId, @RequestParam IntegratedService service) {
        log.info("REST request to get info about a video");
        validateAdminRole();
        VideoInfo videoInfo = rewardService.getVideoInfo(videoId, service);
        return ResponseEntity.ok().body(videoInfo);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addReward(@RequestBody @Valid AddRewardRequest rewardRequest) {
        log.info("REST request to add video reward {}", rewardRequest);
        validateAdminRole();
        rewardService.addReward(rewardRequest);
        return ResponseEntity.ok().body(null);
    }

}
