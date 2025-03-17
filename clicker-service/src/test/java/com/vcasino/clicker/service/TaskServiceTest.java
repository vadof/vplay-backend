package com.vcasino.clicker.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vcasino.clicker.config.IntegratedService;
import com.vcasino.clicker.dto.DateRange;
import com.vcasino.clicker.dto.task.AddTaskRequest;
import com.vcasino.clicker.dto.task.SupportedTaskServices;
import com.vcasino.clicker.dto.task.TaskRewardRequest;
import com.vcasino.clicker.dto.task.TaskDto;
import com.vcasino.clicker.dto.youtube.VideoInfo;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.entity.AccountTaskRewardReceived;
import com.vcasino.clicker.entity.Task;
import com.vcasino.clicker.entity.enums.TaskType;
import com.vcasino.clicker.entity.key.AccountTaskKey;
import com.vcasino.clicker.exception.AppException;
import com.vcasino.clicker.mapper.TaskMapper;
import com.vcasino.clicker.mapper.TaskMapperImpl;
import com.vcasino.clicker.mock.AccountMocks;
import com.vcasino.clicker.mock.TaskMocks;
import com.vcasino.clicker.repository.AccountTaskRewardsReceivedRepository;
import com.vcasino.clicker.repository.TaskRepository;
import com.vcasino.clicker.service.video.YoutubeService;
import com.vcasino.clicker.utils.TimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UNIT tests for {@link TaskService}
 */
@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    YoutubeService youtubeService;

    @Mock
    RedisService redisService;

    @Mock
    TaskRepository taskRepository;

    @Mock
    AccountTaskRewardsReceivedRepository accountTaskRewardsRepository;

    @Spy
    TaskMapper mapper = new TaskMapperImpl();

    @Mock
    AccountService accountService;

    @Captor
    ArgumentCaptor<Task> taskArgumentCaptor;

    @Captor
    ArgumentCaptor<AccountTaskRewardReceived> accountRewardsReceivedArgumentCaptor;

    @InjectMocks
    TaskService taskService;

    @Test
    @DisplayName(value = "Get tasks")
    void getTasks() {
        Account account = AccountMocks.getAccountMock(1L);

        List<Task> taskMocks = TaskMocks.getTaskMocks(5);

        mockAvailableTasks(taskMocks, false);
        when(accountTaskRewardsRepository.findTaskIdsByAccountId(account.getId())).thenReturn(Set.of());

        List<TaskDto> tasks = taskService.getTasks(account.getId());

        assertEquals(5, tasks.size());

        for (TaskDto task : tasks) {
            assertFalse(task.getReceived());
        }

        mockAvailableTasks(taskMocks, true);
        when(accountTaskRewardsRepository.findTaskIdsByAccountId(account.getId())).thenReturn(Set.of(2));

        tasks = taskService.getTasks(account.getId());

        assertEquals(5, tasks.size());

        for (TaskDto task : tasks) {
            if (task.getId().equals(2)) {
                assertTrue(task.getReceived());
            } else {
                assertFalse(task.getReceived());
            }
        }

        verify(taskRepository, times(1)).findAllInInterval(any());
    }

    @Test
    @DisplayName(value = "Get supported services by task type")
    void getSupportedServicesByRewardType() {
        List<SupportedTaskServices> supportedTaskServices = taskService.getSupportedServicesByTaskType();

        for (SupportedTaskServices service : supportedTaskServices) {
            assertNotNull(service.getServices());
            assertNotNull(service.getTaskType());
        }
    }

    @Test
    @DisplayName(value = "Get video info")
    void getVideoInfo() {
        String id = "id";
        VideoInfo mock = new VideoInfo(id, LocalTime.ofSecondOfDay(1));

        when(youtubeService.getVideoInfo(id)).thenReturn(mock);

        VideoInfo videoInfo = taskService.getVideoInfo(id, IntegratedService.YOUTUBE);

        assertEquals(mock.getId(), videoInfo.getId());
        assertEquals(mock.getDuration(), videoInfo.getDuration());

        assertThrows(AppException.class, () -> taskService.getVideoInfo(id, IntegratedService.TELEGRAM));
    }

    @Test
    @DisplayName(value = "Add watch task")
    void addWatchReward() {
        AddTaskRequest addTaskRequest = TaskMocks.getAddTaskRequest(TaskType.WATCH, IntegratedService.YOUTUBE);
        VideoInfo videoInfo = new VideoInfo(addTaskRequest.getId(), LocalTime.ofSecondOfDay(50));
        when(youtubeService.getVideoInfo(addTaskRequest.getId())).thenReturn(videoInfo);

        taskService.addTask(addTaskRequest);

        verify(taskRepository, times(1)).save(taskArgumentCaptor.capture());

        Task savedTask = taskArgumentCaptor.getValue();

        assertTaskCorrect(addTaskRequest, savedTask, videoInfo.getDuration().getSecond(), "https://www.youtube.com/watch?v=");
    }

    @Test
    @DisplayName(value = "Add watch task with unsupported service")
    void addWatchTaskNotSupportedService() {
        AddTaskRequest addTaskRequest = TaskMocks.getAddTaskRequest(TaskType.WATCH, IntegratedService.TELEGRAM);
        assertThrows(AppException.class, () -> taskService.addTask(addTaskRequest));
    }

    @Test
    @DisplayName(value = "Add subscribe youtube task")
    void addSubscribeYoutubeTask() {
        AddTaskRequest addTaskRequest = TaskMocks.getAddTaskRequest(TaskType.SUBSCRIBE, IntegratedService.YOUTUBE);
        taskService.addTask(addTaskRequest);

        verify(taskRepository, times(1)).save(taskArgumentCaptor.capture());
        Task savedTask = taskArgumentCaptor.getValue();

        assertTaskCorrect(addTaskRequest, savedTask, null, "https://www.youtube.com/@");
    }

    @Test
    @DisplayName(value = "Add subscribe telegram task")
    void addSubscribeTelegramTask() {
        AddTaskRequest addTaskRequest = TaskMocks.getAddTaskRequest(TaskType.SUBSCRIBE, IntegratedService.TELEGRAM);
        taskService.addTask(addTaskRequest);
        verify(taskRepository, times(1)).save(taskArgumentCaptor.capture());
        Task savedTask = taskArgumentCaptor.getValue();

        assertTaskCorrect(addTaskRequest, savedTask, null, "https://t.me/");
    }

    @Test
    @DisplayName(value = "Add task updating availableTasks cache")
    void addTaskUpdatingAvailableTasksCache() {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime dayStart = now.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime dayEnd = dayStart.plusDays(1);

        LocalDateTime nowMinus1Minute = now.minusMinutes(1);
        LocalDateTime nowPlus1Minute = now.plusMinutes(1);

        List<DateRange> dateRanges = List.of(
                new DateRange(dayStart, dayEnd), // Caches
                new DateRange(nowMinus1Minute, null), // Caches
                new DateRange(nowPlus1Minute, dayEnd), // Doesn't cache
                new DateRange(nowPlus1Minute, null)  // Doesn't cache
        );

        for (int i = 0; i < dateRanges.size(); i++) {
            AddTaskRequest addTaskRequest = TaskMocks.getAddTaskRequest(TaskType.SUBSCRIBE, IntegratedService.TELEGRAM);
            addTaskRequest.setTaskName("" + i);
            addTaskRequest.setDateRange(dateRanges.get(i));
            taskService.addTask(addTaskRequest);
        }

        verify(taskRepository, times(4)).save(any(Task.class));
        verify(redisService, times(1)).save(eq("available_tasks"), anyList(), any(Long.class));
        verify(redisService, times(1)).save(eq("available_tasks"), anyList());
    }

    @Test
    @DisplayName(value = "Receive task reward")
    void receiveTaskReward() {
        Account account = AccountMocks.getAccountMock(1L);
        Task task = TaskMocks.getTaskMock(2, TaskType.WATCH);

        DateRange dateRange = getDateRange();
        task.setValidFrom(dateRange.getStart());
        task.setEndsIn(dateRange.getEnd());

        when(accountService.getById(account.getId())).thenReturn(account);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(accountTaskRewardsRepository.existsById(new AccountTaskKey(account.getId(), task.getId()))).thenReturn(false);

        LocalDateTime clickTime = dateRange.getStart().plusMinutes(30);

        TaskRewardRequest request = new TaskRewardRequest(task.getId(), clickTime);
        LocalDateTime currentDate = TimeUtil.getCurrentDateTime();
        taskService.receiveTaskReward(account.getId(), request);

        verify(accountTaskRewardsRepository, times(1)).save(accountRewardsReceivedArgumentCaptor.capture());
        verify(accountService, times(1)).addCoins(account, task.getRewardCoins());
        verify(accountService, times(1)).updateAccount(account);
        verify(accountService, times(1)).toDto(account);

        AccountTaskRewardReceived savedRecord = accountRewardsReceivedArgumentCaptor.getValue();
        assertEquals(account.getId(), savedRecord.getAccountId());
        assertEquals(task.getId(), savedRecord.getTaskId());

        boolean dateEqual = savedRecord.getReceivedAt().isEqual(currentDate);
        boolean recordDateBetween = currentDate.isBefore(savedRecord.getReceivedAt())
                && currentDate.plusSeconds(5).isAfter(savedRecord.getReceivedAt());

        assertTrue(dateEqual || recordDateBetween);
    }

    @Test
    @DisplayName(value = "Receive same award twice")
    void receiveRewardTwice() {
        Account account = AccountMocks.getAccountMock(1L);
        Task task = TaskMocks.getTaskMock(1, TaskType.WATCH);

        DateRange dateRange = getDateRange();
        task.setValidFrom(dateRange.getStart());
        task.setEndsIn(dateRange.getEnd());

        when(accountService.getById(account.getId())).thenReturn(account);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(accountTaskRewardsRepository.existsById(new AccountTaskKey(account.getId(), task.getId()))).thenReturn(true);

        LocalDateTime clickTime = dateRange.getStart().plusMinutes(30);

        TaskRewardRequest request = new TaskRewardRequest(task.getId(), clickTime);

        assertThrows(AppException.class, () -> taskService.receiveTaskReward(account.getId(), request));
    }

    @Test
    @DisplayName(value = "Receive reward before it has appeared")
    void receiveRewardBeforeItAppeared() {
        Account account = AccountMocks.getAccountMock(1L);
        Task task = TaskMocks.getTaskMock(1, TaskType.WATCH);

        DateRange dateRange = getDateRange();
        task.setValidFrom(dateRange.getStart());
        task.setEndsIn(dateRange.getEnd());

        when(accountService.getById(account.getId())).thenReturn(account);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(accountTaskRewardsRepository.existsById(new AccountTaskKey(account.getId(), task.getId()))).thenReturn(false);

        LocalDateTime clickTime = dateRange.getStart().minusSeconds(1);

        TaskRewardRequest request = new TaskRewardRequest(task.getId(), clickTime);

        assertThrows(AppException.class, () -> taskService.receiveTaskReward(account.getId(), request));
    }

    @Test
    @DisplayName(value = "Receive reward after it has disappeared")
    void receiveRewardAfterItDisappeared() {
        Account account = AccountMocks.getAccountMock(1L);
        Task task = TaskMocks.getTaskMock(1, TaskType.WATCH);

        DateRange dateRange = getDateRange();
        task.setValidFrom(dateRange.getStart());
        task.setEndsIn(dateRange.getEnd());

        when(accountService.getById(account.getId())).thenReturn(account);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(accountTaskRewardsRepository.existsById(new AccountTaskKey(account.getId(), task.getId()))).thenReturn(false);

        LocalDateTime clickTime = dateRange.getStart().minusSeconds(1);

        TaskRewardRequest request = new TaskRewardRequest(task.getId(), clickTime);

        assertThrows(AppException.class, () -> taskService.receiveTaskReward(account.getId(), request));
    }

    @Test
    @DisplayName(value = "Receive reward, click after current date")
    void receiveRewardClickAfterCurrentDate() {
        Account account = AccountMocks.getAccountMock(1L);
        Task task = TaskMocks.getTaskMock(1, TaskType.WATCH);

        DateRange dateRange = getDateRange();
        task.setValidFrom(dateRange.getStart());
        task.setEndsIn(dateRange.getEnd());
        LocalDateTime now = dateRange.getStart().plusHours(1);

        when(accountService.getById(account.getId())).thenReturn(account);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(accountTaskRewardsRepository.existsById(new AccountTaskKey(account.getId(), task.getId()))).thenReturn(false);

        LocalDateTime clickTime = now.plusMinutes(1);

        TaskRewardRequest request = new TaskRewardRequest(task.getId(), clickTime);

        try (MockedStatic<TimeUtil> ignored = mockStatic(TimeUtil.class)) {
            mockTime(now, clickTime);
            assertThrows(AppException.class, () -> taskService.receiveTaskReward(account.getId(), request));
        }

    }

    @Test
    @DisplayName(value = "Receive reward, click no more than one day ago")
    void receiveRewardClickNoMoreThanOneDayAgo() {
        Account account = AccountMocks.getAccountMock(1L);
        Task task = TaskMocks.getTaskMock(1, TaskType.WATCH);

        DateRange dateRange = getDateRange();
        task.setValidFrom(dateRange.getStart());
        task.setEndsIn(dateRange.getEnd());
        LocalDateTime now = dateRange.getEnd().plusHours(24);

        when(accountService.getById(account.getId())).thenReturn(account);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(accountTaskRewardsRepository.existsById(new AccountTaskKey(account.getId(), task.getId()))).thenReturn(false);

        LocalDateTime clickTime = dateRange.getEnd().minusMinutes(1);

        TaskRewardRequest request = new TaskRewardRequest(task.getId(), clickTime);

        try (MockedStatic<TimeUtil> ignored = mockStatic(TimeUtil.class)) {
            mockTime(now, clickTime);
            assertThrows(AppException.class, () -> taskService.receiveTaskReward(account.getId(), request));
        }
    }

    @Test
    @DisplayName(value = "Receive reward, without watching video")
    void receiveRewardWithoutWatchingVideo() {
        Account account = AccountMocks.getAccountMock(1L);
        Task task = TaskMocks.getTaskMock(1, TaskType.WATCH);
        task.setDurationInSeconds(3 * 60);

        DateRange dateRange = getDateRange();
        task.setValidFrom(dateRange.getStart());
        task.setEndsIn(dateRange.getEnd());
        LocalDateTime now = dateRange.getStart().plusHours(1);

        when(accountService.getById(account.getId())).thenReturn(account);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(accountTaskRewardsRepository.existsById(new AccountTaskKey(account.getId(), task.getId()))).thenReturn(false);

        LocalDateTime clickTime = now.minusMinutes(1);

        TaskRewardRequest request = new TaskRewardRequest(task.getId(), clickTime);

        try (MockedStatic<TimeUtil> ignored = mockStatic(TimeUtil.class)) {
            mockTime(now, clickTime);
            assertThrows(AppException.class, () -> taskService.receiveTaskReward(account.getId(), request));
        }
    }

    private void mockAvailableTasks(List<Task> tasks, boolean redisCache) {
        if (redisCache) {
            when(redisService.get(eq("available_tasks"), any(TypeReference.class))).thenReturn(tasks);
        } else {
            when(redisService.get(eq("available_tasks"), any(TypeReference.class))).thenReturn(null);
            when(taskRepository.findAllInInterval(any())).thenReturn(tasks);
        }
    }

    private DateRange getDateRange() {
        LocalDateTime now = TimeUtil.getCurrentDateTime();
        LocalDateTime start = now.minusDays(1);
        LocalDateTime end = now.plusDays(1);
        return new DateRange(start, end);
    }

    private void mockTime(LocalDateTime now, LocalDateTime clickTime) {
        when(TimeUtil.getCurrentDateTime()).thenReturn(now);
        when(TimeUtil.getDifferenceInSeconds(clickTime, now))
                .thenReturn(Duration.between(clickTime.withNano(0), now.withNano(0)).getSeconds());
    }


    private void assertTaskCorrect(AddTaskRequest request, Task savedTask, Integer durationInSeconds, String baseLink) {
        assertEquals(durationInSeconds, savedTask.getDurationInSeconds());
        assertEquals(request.getService(), savedTask.getIntegratedService());
        assertEquals(baseLink + request.getId(), savedTask.getLink());
        assertEquals(request.getTaskType(), savedTask.getType());
        assertEquals(request.getDateRange().getStart(), savedTask.getValidFrom());
        assertEquals(request.getDateRange().getEnd(), savedTask.getEndsIn());
        assertEquals(request.getRewardCoins(), savedTask.getRewardCoins());
        assertEquals(request.getTaskName(), savedTask.getName());
    }
}
