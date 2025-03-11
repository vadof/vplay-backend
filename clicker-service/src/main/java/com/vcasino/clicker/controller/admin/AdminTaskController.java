package com.vcasino.clicker.controller.admin;

import com.vcasino.clicker.config.IntegratedService;
import com.vcasino.clicker.controller.common.GenericController;
import com.vcasino.clicker.dto.task.AddTaskRequest;
import com.vcasino.clicker.dto.task.SupportedTaskServices;
import com.vcasino.clicker.dto.task.TaskUpdateRequest;
import com.vcasino.clicker.dto.youtube.VideoInfo;
import com.vcasino.clicker.service.TaskService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Hidden
@RestController
@RequestMapping("/api/v1/clicker/admin/tasks")
@Validated
@Slf4j
public class AdminTaskController extends GenericController {

    private final TaskService taskService;

    public AdminTaskController(HttpServletRequest request, TaskService taskService) {
        super(request);
        this.taskService = taskService;
    }

    @GetMapping(value = "/properties", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SupportedTaskServices>> getSupportedServicesByTaskType() {
        log.info("REST request to get supported services by task type");
        validateAdminRole();
        return ResponseEntity.ok().body(taskService.getSupportedServicesByTaskType());
    }

    @GetMapping(value = "/video-info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VideoInfo> getVideoInfo(@RequestParam String videoId, @RequestParam IntegratedService service) {
        log.info("REST request to get info about a video");
        validateAdminRole();
        VideoInfo videoInfo = taskService.getVideoInfo(videoId, service);
        return ResponseEntity.ok().body(videoInfo);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addTask(@RequestBody @Valid AddTaskRequest taskRequest) {
        log.info("REST request to add task {}", taskRequest);
        validateAdminRole();
        taskService.addTask(taskRequest);
        return ResponseEntity.ok().body(null);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addTask(@PathVariable(name = "id") Integer id, @RequestBody @Valid TaskUpdateRequest updateRequest) {
        log.info("REST request to update Task#{} - {}", id, updateRequest);
        validateAdminRole();
        taskService.updateTask(id, updateRequest);
        return ResponseEntity.ok().body(null);
    }
}
