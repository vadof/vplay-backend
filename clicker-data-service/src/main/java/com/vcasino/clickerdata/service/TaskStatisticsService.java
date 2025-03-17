package com.vcasino.clickerdata.service;

import com.vcasino.clickerdata.dto.TaskInformation;
import com.vcasino.clickerdata.exception.AppException;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class TaskStatisticsService {

    private final JdbcTemplate jdbcTemplate;
    private final String TASK_QUERY = """
            SELECT t.id,
                   t.type,
                   t.name,
                   t.link,
                   t.duration_seconds as durationInSeconds,
                   t.service_name,
                   t.reward_coins,
                   t.valid_from,
                   t.ends_in,
                   t.created_at,
                   (SELECT COUNT(*) FROM account_completed_tasks act where act.task_id = t.id) as completedTimes
            FROM task t
            """;

    public List<TaskInformation> getActiveTasks() {
        String query = TASK_QUERY + " WHERE t.valid_from <= CURRENT_DATE AND (t.ends_in IS NULL OR t.ends_in > CURRENT_DATE) ORDER BY t.id";
        List<TaskInformation> taskInformation = jdbcTemplate.query(query, new BeanPropertyRowMapper<>(TaskInformation.class));
        setActive(taskInformation);
        return taskInformation;
    }

    public List<TaskInformation> getRecentlyCreatedTasks() {
        String query = TASK_QUERY + " ORDER BY t.created_at DESC LIMIT 10";
        List<TaskInformation> taskInformation = jdbcTemplate.query(query, new BeanPropertyRowMapper<>(TaskInformation.class));
        setActive(taskInformation);
        return taskInformation;
    }

    public List<TaskInformation> getTaskInformation(Long taskId, String taskName, String linkId) {
        String query;
        Object argument;

        if (taskId != null) {
            query = TASK_QUERY + " WHERE t.id = ?";
            argument = taskId;
        } else if (Strings.isNotEmpty(taskName)) {
            query = TASK_QUERY + " WHERE t.name LIKE CONCAT('%', ?, '%')";
            argument = taskName;
        } else {
            query = TASK_QUERY + " WHERE t.link LIKE CONCAT('%', ?, '%')";
            argument = linkId;
        }

        List<TaskInformation> taskInformation = jdbcTemplate.query(query, new BeanPropertyRowMapper<>(TaskInformation.class), argument);

        if (taskInformation.isEmpty()) {
            throw new AppException("Task not found", HttpStatus.NOT_FOUND);
        }

        setActive(taskInformation);

        return taskInformation;
    }

    private void setActive(List<TaskInformation> taskInformationList) {
        if (taskInformationList.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();

        for (TaskInformation task : taskInformationList) {
            boolean isActive = task.getValidFrom().isBefore(now) || task.getValidFrom().isEqual(now);

            if (task.getEndsIn() != null) {
                isActive = isActive && task.getEndsIn().isAfter(now);
            }

            task.setActive(isActive);
        }
    }


}
