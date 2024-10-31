package com.vcasino.clicker.controller;

import com.vcasino.clicker.controller.common.GenericController;
import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.dto.admin.AccountImprove;
import com.vcasino.clicker.dto.youtube.VideoInfo;
import com.vcasino.clicker.service.YoutubeService;
import com.vcasino.clicker.service.admin.AdminService;
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

@Hidden
@RestController
@RequestMapping("/api/v1/clicker/admin")
@Validated
@Slf4j
public class AdminController extends GenericController {

    private final AdminService adminService;
    private final YoutubeService youtubeService;

    public AdminController(HttpServletRequest request, AdminService adminService, YoutubeService youtubeService) {
        super(request);
        this.adminService = adminService;
        this.youtubeService = youtubeService;
    }

    @PostMapping(value = "/improve",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> improveAccount(@RequestBody @Valid AccountImprove accountImprove) {
        log.info("REST request to improve Account#{}", accountImprove.getAccountId());
        validateAdminRole();
        AccountDto account = adminService.improveAccount(accountImprove);
        return ResponseEntity.ok().body(account);
    }

    @GetMapping(value = "/video", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VideoInfo> getVideo(@RequestParam String videoId) {
        log.info("REST request to get info about a video");
        validateAdminRole();
        VideoInfo videoInfo = youtubeService.getVideoInfo(videoId);
        return ResponseEntity.ok().body(videoInfo);
    }

}
