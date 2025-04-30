package com.vcasino.clicker.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vcasino.clicker.config.IntegratedService;
import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.dto.DateRange;
import com.vcasino.clicker.dto.task.AddTaskRequest;
import com.vcasino.clicker.dto.task.SupportedTaskServices;
import com.vcasino.clicker.dto.task.TaskDto;
import com.vcasino.clicker.dto.task.TaskRewardRequest;
import com.vcasino.clicker.dto.task.TaskUpdateRequest;
import com.vcasino.clicker.dto.youtube.VideoInfo;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.entity.AccountCompletedTasks;
import com.vcasino.clicker.entity.Task;
import com.vcasino.clicker.entity.enums.TaskType;
import com.vcasino.clicker.entity.key.AccountTaskKey;
import com.vcasino.clicker.exception.AppException;
import com.vcasino.clicker.mapper.TaskMapper;
import com.vcasino.clicker.repository.AccountTaskCompletedRepository;
import com.vcasino.clicker.repository.TaskRepository;
import com.vcasino.clicker.service.video.YoutubeService;
import com.vcasino.clicker.utils.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class TaskService {

    private final RedisService redisService;
    private static final String TASK_CACHE_KEY = "available_tasks";
    private static final TypeReference<List<Task>> TASK_LIST_TYPE = new TypeReference<>() {};

    private final YoutubeService youtubeService;
    private final TaskRepository taskRepository;
    private final AccountTaskCompletedRepository accountTasksRepository;
    private final TaskMapper mapper;
    private final AccountService accountService;

    public List<TaskDto> getTasks(Long accountId) {
        List<Task> availableTasks = getAvailableTasks();
        Set<Integer> completedTasksIdsByUser = accountTasksRepository.findTaskIdsByAccountId(accountId);

        List<TaskDto> taskDtos = mapper.toDtos(availableTasks);
        taskDtos.forEach(r -> r.setReceived(completedTasksIdsByUser.contains(r.getId())));
        return taskDtos;
    }

    private List<Task> getAvailableTasks() {
        List<Task> tasks = redisService.get(TASK_CACHE_KEY, TASK_LIST_TYPE);
        if (tasks != null) {
            return tasks;
        }

        tasks = taskRepository.findAllInInterval(TimeUtil.getCurrentDateTime());
        return cacheAvailableTasks(tasks);
    }

    private List<Task> cacheAvailableTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return tasks;
        }

        LocalDateTime now = TimeUtil.getCurrentDateTime();
        LocalDateTime closestEndsIn = tasks.stream()
                .filter(task -> task.getEndsIn() != null && task.getEndsIn().isAfter(now))
                .map(Task::getEndsIn)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        if (closestEndsIn != null) {
            long ttlSeconds = Duration.between(now, closestEndsIn).getSeconds();
            long oneWeekInSeconds = 604800;
            redisService.save(TASK_CACHE_KEY, tasks, Math.min(ttlSeconds, oneWeekInSeconds));
        } else {
            redisService.save(TASK_CACHE_KEY, tasks);
        }

        return tasks;
    }

    public List<SupportedTaskServices> getSupportedServicesByTaskType() {
        List<SupportedTaskServices> supportedTaskServices = new ArrayList<>();

        for (TaskType taskType : TaskType.values()) {
            List<IntegratedService> services = new ArrayList<>();

            for (IntegratedService service : IntegratedService.values()) {
                if (taskType.serviceIsSupported(service)) {
                    services.add(service);
                }
            }

            supportedTaskServices.add(new SupportedTaskServices(taskType, services));
        }

        return supportedTaskServices;
    }

    public VideoInfo getVideoInfo(String videoId, IntegratedService integratedService) {
        return switch (integratedService) {
            case YOUTUBE -> youtubeService.getVideoInfo(videoId);
            default -> throw new AppException(integratedService.value() + " doesn't support watching", HttpStatus.BAD_REQUEST);
        };
    }

    public void addTask(AddTaskRequest request) {
        Task task = switch (request.getTaskType()) {
            case WATCH -> addWatchTask(request);
            case SUBSCRIBE -> addSubscribeTask(request);
        };

        taskRepository.save(task);

        log.info("Task#{} saved in the database", task.getId());

        LocalDateTime now = TimeUtil.getCurrentDateTime();
        if ((task.getValidFrom().isBefore(now) || task.getValidFrom().isEqual(now))
                && (task.getEndsIn() == null || task.getEndsIn().isAfter(now))
        ) {
            List<Task> tasks = redisService.get(TASK_CACHE_KEY, TASK_LIST_TYPE);
            if (tasks == null) {
                tasks = new ArrayList<>();
            }
            tasks.add(task);
            cacheAvailableTasks(tasks);
        }
    }

    @Transactional
    public void updateTask(Integer taskId, TaskUpdateRequest updateRequest) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException("Task not found", HttpStatus.NOT_FOUND));

        task.setName(updateRequest.getName());
        task.setRewardCoins(updateRequest.getRewardCoins());

        DateRange dateRange = updateRequest.getDateRange();
        boolean startUpdated = !dateRange.getStart().equals(task.getValidFrom());
        boolean endUpdated = (dateRange.getEnd() != null && task.getEndsIn() != null && !dateRange.getEnd().equals(task.getEndsIn()))
                || (dateRange.getEnd() == null && task.getEndsIn() != null)
                || (dateRange.getEnd() != null && task.getEndsIn() == null);

        if (startUpdated || endUpdated) {
            if (dateRange.getEnd() != null && dateRange.getEnd().isBefore(dateRange.getStart())) {
                throw new AppException("The end date cannot be earlier than the start date", HttpStatus.BAD_REQUEST);
            }

            task.setValidFrom(dateRange.getStart());
            task.setEndsIn(dateRange.getEnd());
        }

        task.setName(updateRequest.getName());
        task.setRewardCoins(updateRequest.getRewardCoins());

        taskRepository.save(task);
        cacheAvailableTasks(taskRepository.findAllInInterval(LocalDateTime.now()));

        log.info("Task#{} updated", task.getId());
    }

    private Task addWatchTask(AddTaskRequest request) {
        Task task = new Task();
        setCommonFields(task, request);
        task.setLink(getWatchLink(request.getId(), request.getService()));
        task.setDurationInSeconds(getVideoInfo(request.getId(), request.getService()).getDuration().toSecondOfDay());
        return task;
    }

    private String getWatchLink(String id, IntegratedService integratedService) {
        return switch (integratedService) {
            case YOUTUBE -> "https://www.youtube.com/watch?v=" + id;
            default -> throw new AppException(integratedService.value() + " doesn't support watching", HttpStatus.BAD_REQUEST);
        };
    }

    private Task addSubscribeTask(AddTaskRequest request) {
        Task task = new Task();
        setCommonFields(task, request);
        task.setLink(getSubscribeLink(request.getId(), request.getService()));
        task.setDurationInSeconds(null);
        return task;
    }

    private void setCommonFields(Task task, AddTaskRequest request) {
        task.setType(request.getTaskType());
        task.setName(request.getTaskName());
        task.setIntegratedService(request.getService());
        task.setRewardCoins(request.getRewardCoins());
        task.setValidFrom(request.getDateRange().getStart());
        task.setEndsIn(request.getDateRange().getEnd());
        task.setCreatedAt(LocalDateTime.now());
    }

    private String getSubscribeLink(String id, IntegratedService integratedService) {
        return switch (integratedService) {
            case YOUTUBE -> "https://www.youtube.com/@" + id;
            case TELEGRAM -> "https://t.me/" + id;
        };
    }

    @Transactional
    public AccountDto receiveTaskReward(Long accountId, TaskRewardRequest taskRequest) {
        Task task = taskRepository.findById(taskRequest.getTaskId())
                .orElseThrow(() -> new AppException(null, HttpStatus.FORBIDDEN));

        Account account = accountService.getById(accountId);

        boolean rewardAlreadyReceived = accountTasksRepository.existsById(new AccountTaskKey(accountId, task.getId()));
        if (rewardAlreadyReceived) {
            throw new AppException("Task reward already received", HttpStatus.BAD_REQUEST);
        }

        validateClickTime(taskRequest.getClickTime(), task);

        AccountCompletedTasks taskCompleted = AccountCompletedTasks.builder()
                .accountId(accountId)
                .taskId(task.getId())
                .completedAt(TimeUtil.getCurrentDateTime())
                .build();

        accountTasksRepository.save(taskCompleted);
        accountService.addCoins(account, task.getRewardCoins());
        accountService.updateAccount(account);

        log.debug("Account#{} received reward for completed Task#{}", accountId, task.getId());

        return accountService.toDto(account);
    }

    private void validateClickTime(LocalDateTime clickTime, Task task) {
        LocalDateTime now = TimeUtil.getCurrentDateTime();

        boolean clickAfterTaskAppeared = clickTime.isAfter(task.getValidFrom());
        boolean clickBeforeTaskExpired = task.getEndsIn() == null ||
                clickTime.isBefore(task.getEndsIn());
        boolean clickBeforeCurrentDate = clickTime.isBefore(now);
        boolean clickWasNoMoreThanOneDayAgo = clickTime.isAfter(now.minusDays(1));

        if (clickAfterTaskAppeared && clickBeforeTaskExpired
                && clickBeforeCurrentDate && clickWasNoMoreThanOneDayAgo) {
            long secondsSinceClick = TimeUtil.getDifferenceInSeconds(clickTime, now);
            switch (task.getType()) {
                case SUBSCRIBE -> {}
                case WATCH -> {
                    if (secondsSinceClick < task.getDurationInSeconds()) {
                        throw new AppException("Video not watched", HttpStatus.BAD_REQUEST);
                    }
                }
                default -> log.debug("Please add logic for a new TaskType");
            }
        } else {
            throw new AppException("Task expired", HttpStatus.BAD_REQUEST);
        }
    }
}
