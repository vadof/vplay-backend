package com.vcasino.clicker.controller;

import com.vcasino.clicker.controller.common.GenericController;
import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.dto.streak.StreakInfo;
import com.vcasino.clicker.dto.task.TaskDto;
import com.vcasino.clicker.dto.task.TaskRewardRequest;
import com.vcasino.clicker.service.StreakService;
import com.vcasino.clicker.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Tasks", description = "API operations with Tasks")
@RestController
@RequestMapping("/api/v1/clicker/tasks")
@Validated
@Slf4j
public class TaskController extends GenericController {

    private final StreakService streakService;
    private final TaskService taskService;

    public TaskController(HttpServletRequest request, StreakService streakService, TaskService taskService) {
        super(request);
        this.streakService = streakService;
        this.taskService = taskService;
    }

    @Operation(summary = "Get tasks info")
    @ApiResponse(responseCode = "200", description = "Return tasks info",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
            @Schema(implementation = TaskDto.class)))
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskDto>> getTasksInfo() {
        log.debug("REST request to get tasks info");
        List<TaskDto> tasks = taskService.getTasks(getAccountId());
        return ResponseEntity.ok().body(tasks);
    }

    @Operation(summary = "Receive reward for completed task")
    @ApiResponse(responseCode = "200", description = "Reward received",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
            @Schema(implementation = AccountDto.class)))
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> receiveTaskReward(@RequestBody @Valid TaskRewardRequest taskRequest) {
        log.debug("REST request to complete task");
        AccountDto accountDto = taskService.receiveTaskReward(getAccountId(), taskRequest);
        return ResponseEntity.ok().body(accountDto);
    }

    @Operation(summary = "Get streak info")
    @ApiResponse(responseCode = "200", description = "Return streaks",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
            @Schema(implementation = StreakInfo.class)))
    @GetMapping(value = "/streaks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreakInfo> getStreakInfo() {
        log.debug("REST request to get StreakInfo");
        StreakInfo streakInfo = streakService.getStreakInfo(getAccountId());
        return ResponseEntity.ok().body(streakInfo);
    }

    @Operation(summary = "Receive reward for completed streak")
    @ApiResponse(responseCode = "200", description = "Reward received",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
            @Schema(implementation = AccountDto.class)))
    @PostMapping(value = "/streaks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> receiveStreakReward() {
        log.debug("REST request to receive streak reward");
        AccountDto accountDto = streakService.receiveReward(getAccountId());
        return ResponseEntity.ok().body(accountDto);
    }

}
