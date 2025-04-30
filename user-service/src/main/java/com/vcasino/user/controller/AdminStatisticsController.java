package com.vcasino.user.controller;

import com.vcasino.user.dto.UserStatisticsDto;
import com.vcasino.user.exception.AppException;
import com.vcasino.user.repository.UserRepository;
import com.vcasino.user.service.StatisticsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/admin/statistics")
@AllArgsConstructor
@Slf4j
public class AdminStatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserStatisticsDto> getStatistics() {
        log.debug("REST request to get Statistics");
        UserStatisticsDto statistics = statisticsService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserRepository.UserInformation> getUserInformation(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String username) {
        if (id == null && (username == null || Strings.isEmpty(username))) {
            throw new AppException("Either 'id' or 'username' must be provided", HttpStatus.BAD_REQUEST);
        }

        log.debug("REST request to get User information");
        UserRepository.UserInformation information = statisticsService.getUserInformation(id, username);
        return ResponseEntity.ok(information);
    }

}
