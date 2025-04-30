package com.vcasino.clickerdata.controller;

import com.vcasino.clickerdata.dto.ChartData;
import com.vcasino.clickerdata.dto.ChartOption;
import com.vcasino.clickerdata.dto.GeneralStatistics;
import com.vcasino.clickerdata.service.GeneralStatisticsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clicker-data/admin/statistics/general")
@AllArgsConstructor
@Slf4j
public class GeneralStatisticsController {

    private final GeneralStatisticsService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeneralStatistics> getGeneralStatistics() {
        log.debug("REST request to get General Statistics");
        GeneralStatistics generalStatistics = service.getGeneralStatistics();
        return ResponseEntity.ok(generalStatistics);
    }

    @GetMapping(value = "/charts/activeUsers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChartData<String, Integer>> getActiveUsersChart(
            @RequestParam(name = "chartOption") ChartOption chartOption) {
        log.debug("REST request to get Active Users Chart with option: {}", chartOption);
        ChartData<String, Integer> activeUsersChart = service.getActiveUsersChart(chartOption);
        return ResponseEntity.ok(activeUsersChart);
    }

    @GetMapping(value = "/charts/totalClicks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChartData<String, Long>> getTotalClicksChart(
            @RequestParam(name = "chartOption") ChartOption chartOption) {
        log.debug("REST request to get Total Clicks Chart with option: {}", chartOption);
        ChartData<String, Long> totalClicksChart = service.getTotalClicksChart(chartOption);
        return ResponseEntity.ok(totalClicksChart);
    }

}
