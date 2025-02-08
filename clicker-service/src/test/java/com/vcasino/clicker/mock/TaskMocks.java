package com.vcasino.clicker.mock;

import com.vcasino.clicker.config.IntegratedService;
import com.vcasino.clicker.dto.DateRange;
import com.vcasino.clicker.dto.task.AddTaskRequest;
import com.vcasino.clicker.entity.Task;
import com.vcasino.clicker.entity.enums.TaskType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskMocks {
    public static Task getTaskMock(Integer id, TaskType rewardType) {
        return Task.builder()
                .id(id)
                .type(rewardType)
                .name("Take reward")
                .link("https://youtube.com/watch?v" + id)
                .integratedService(IntegratedService.YOUTUBE)
                .durationInSeconds(rewardType == TaskType.WATCH ? 10 : 0)
                .rewardCoins(1000)
                .validFrom(LocalDateTime.of(2024, 1, 1, 0, 0))
                .endsIn(null)
                .build();
    }

    public static List<Task> getTaskMocks(int n) {
        List<Task> rewards = new ArrayList<>();
        for (int i = 1; i < n + 1; i++) {
            rewards.add(getTaskMock(i, i % 2 == 0 ? TaskType.SUBSCRIBE : TaskType.WATCH));
        }
        return rewards;
    }

    public static AddTaskRequest getAddTaskRequest(TaskType type, IntegratedService service) {
        return AddTaskRequest.builder()
                .id("id")
                .taskType(type)
                .service(service)
                .rewardCoins(100)
                .taskName("name")
                .dateRange(new DateRange(LocalDateTime.of(2024, 1, 1, 0, 0), null))
                .build();
    }
}
