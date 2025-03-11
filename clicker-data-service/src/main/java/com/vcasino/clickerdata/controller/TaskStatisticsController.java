package com.vcasino.clickerdata.controller;

import com.vcasino.clickerdata.dto.TaskInformation;
import com.vcasino.clickerdata.exception.AppException;
import com.vcasino.clickerdata.service.TaskStatisticsService;
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
@RequestMapping("/api/v1/clicker-data/admin/statistics/tasks")
@AllArgsConstructor
@Slf4j
public class TaskStatisticsController {

    private final TaskStatisticsService service;

    @GetMapping(value = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskInformation>> getActiveTasks() {
        log.info("REST request to get active Tasks");
        List<TaskInformation> tasks = service.getActiveTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping(value = "/recent", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskInformation>> getRecentlyCreatedTasks() {
        log.info("REST request to get recently added Tasks");
        List<TaskInformation> tasks = service.getRecentlyCreatedTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskInformation>> getActiveTasks(@RequestParam(name = "taskId", required = false) Long taskId,
                                                          @RequestParam(name = "taskName", required = false) String taskName,
                                                          @RequestParam(name = "linkId", required = false) String linkId) {
        if (taskId == null && Strings.isEmpty(taskName) && Strings.isEmpty(linkId)) {
            throw new AppException("Either 'taskId', 'taskName' or 'linkId' must be provided", HttpStatus.BAD_REQUEST);
        }

        log.info("REST request to get Task");
        List<TaskInformation> task = service.getTaskInformation(taskId, taskName, linkId);
        return ResponseEntity.ok(task);
    }

}
