package com.vcasino.clickerdata.controller;

import com.vcasino.clickerdata.dto.AccountInformation;
import com.vcasino.clickerdata.dto.ChartData;
import com.vcasino.clickerdata.dto.ChartOption;
import com.vcasino.clickerdata.dto.TopAccount;
import com.vcasino.clickerdata.exception.AppException;
import com.vcasino.clickerdata.service.AccountStatisticsService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/clicker-data/admin/statistics/accounts")
@AllArgsConstructor
@Slf4j
public class AccountStatisticsController {

    private final AccountStatisticsService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountInformation> getAccountInformation(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String username) {
        if (id == null && Strings.isEmpty(username)) {
            throw new AppException("Either 'id' or 'username' must be provided", HttpStatus.BAD_REQUEST);
        }

        log.info("REST request to get User information");
        AccountInformation information = service.getAccountInformation(id, username);
        return ResponseEntity.ok(information);
    }

    @GetMapping(value = "/charts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChartData<String, Integer>> getAccountClicksChart(
            @RequestParam(name = "accountId") Long accountId,
            @RequestParam(name = "chartOption") ChartOption chartOption) {
        log.info("REST request to get Account Clicks Chart with option: {}", chartOption);
        ChartData<String, Integer> chart = service.getAccountClicksChart(accountId, chartOption);
        return ResponseEntity.ok(chart);
    }

    @GetMapping(value = "/top", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TopAccount>> getTop10Accounts() {
        log.info("REST request to get Top 10 Accounts");
        List<TopAccount> topAccounts = service.getTop10Accounts();
        return ResponseEntity.ok(topAccounts);
    }

}
