package com.vcasino.clicker.service;

import com.vcasino.clicker.config.IntegratedService;
import com.vcasino.clicker.dto.DateRange;
import com.vcasino.clicker.dto.reward.AddRewardRequest;
import com.vcasino.clicker.dto.reward.ReceiveRewardRequest;
import com.vcasino.clicker.dto.reward.RewardDto;
import com.vcasino.clicker.dto.youtube.VideoInfo;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.entity.AccountRewardsReceived;
import com.vcasino.clicker.entity.Reward;
import com.vcasino.clicker.entity.enums.RewardType;
import com.vcasino.clicker.entity.id.key.AccountRewardKey;
import com.vcasino.clicker.exception.AppException;
import com.vcasino.clicker.mapper.RewardMapper;
import com.vcasino.clicker.mapper.RewardMapperImpl;
import com.vcasino.clicker.mock.AccountMocks;
import com.vcasino.clicker.mock.RewardMocks;
import com.vcasino.clicker.repository.AccountRewardsReceivedRepository;
import com.vcasino.clicker.repository.RewardRepository;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UNIT tests for {@link RewardService}
 */
@ExtendWith(MockitoExtension.class)
public class RewardServiceTest {

    @Mock
    YoutubeService youtubeService;

    @Mock
    RewardRepository rewardRepository;

    @Mock
    AccountRewardsReceivedRepository accountRewardsRepository;

    @Spy
    RewardMapper mapper = new RewardMapperImpl();

    @Mock
    AccountService accountService;

    @Captor
    ArgumentCaptor<Reward> rewardArgumentCaptor;

    @Captor
    ArgumentCaptor<AccountRewardsReceived> accountRewardsReceivedArgumentCaptor;

    @InjectMocks
    RewardService rewardService;

    @Test
    @DisplayName(value = "Get rewards")
    void getRewards() {
        Account account = AccountMocks.getAccountMock(1L);

        List<Reward> rewardMocks = RewardMocks.getRewardMocks(5);
        when(rewardRepository.findAllInInterval(any())).thenReturn(rewardMocks);

        when(accountRewardsRepository.findRewardIdsByAccountId(account.getId())).thenReturn(Set.of());

        List<RewardDto> rewards = rewardService.getRewards(account.getId());

        assertEquals(5, rewards.size());

        for (RewardDto reward : rewards) {
            assertFalse(reward.getReceived());
        }

        when(rewardRepository.findAllInInterval(any())).thenReturn(rewardMocks);
        when(accountRewardsRepository.findRewardIdsByAccountId(account.getId())).thenReturn(Set.of(2));

        rewards = rewardService.getRewards(account.getId());

        assertEquals(5, rewards.size());

        for (RewardDto reward : rewards) {
            if (reward.getId().equals(2)) {
                assertTrue(reward.getReceived());
            } else {
                assertFalse(reward.getReceived());
            }
        }
    }

    @Test
    @DisplayName(value = "Get supported services by reward type")
    void getSupportedServicesByRewardType() {
        Map<RewardType, List<IntegratedService>> res = rewardService.getSupportedServicesByRewardType();

        for (var entry : res.entrySet()) {
            assertNotNull(entry.getKey());
            assertTrue(entry.getValue() != null && !entry.getValue().isEmpty());
        }
    }

    @Test
    @DisplayName(value = "Get video info")
    void getVideoInfo() {
        String id = "id";
        VideoInfo mock = new VideoInfo(id, LocalTime.ofSecondOfDay(1));

        when(youtubeService.getVideoInfo(id)).thenReturn(mock);

        VideoInfo videoInfo = rewardService.getVideoInfo(id, IntegratedService.YOUTUBE);

        assertEquals(mock.getId(), videoInfo.getId());
        assertEquals(mock.getDuration(), videoInfo.getDuration());

        assertThrows(AppException.class, () -> rewardService.getVideoInfo(id, IntegratedService.TELEGRAM));
    }

    @Test
    @DisplayName(value = "Add watch reward")
    void addWatchReward() {
        AddRewardRequest addRewardRequest = RewardMocks.getRewardRequest(RewardType.WATCH, IntegratedService.YOUTUBE);
        VideoInfo videoInfo = new VideoInfo(addRewardRequest.getId(), LocalTime.ofSecondOfDay(50));
        when(youtubeService.getVideoInfo(addRewardRequest.getId())).thenReturn(videoInfo);

        rewardService.addReward(addRewardRequest);

        verify(rewardRepository, times(1)).save(rewardArgumentCaptor.capture());

        Reward savedReward = rewardArgumentCaptor.getValue();

        assertRewardCorrect(addRewardRequest, savedReward, videoInfo.getDuration().getSecond(), "https://www.youtube.com/watch?v=");
    }

    @Test
    @DisplayName(value = "Add watch reward with unsupported service")
    void addWatchRewardNotSupportedService() {
        AddRewardRequest addRewardRequest = RewardMocks.getRewardRequest(RewardType.WATCH, IntegratedService.TELEGRAM);
        assertThrows(AppException.class, () -> rewardService.addReward(addRewardRequest));
    }

    @Test
    @DisplayName(value = "Add subscribe youtube reward")
    void addSubscribeYoutubeReward() {
        AddRewardRequest addRewardRequest = RewardMocks.getRewardRequest(RewardType.SUBSCRIBE, IntegratedService.YOUTUBE);
        rewardService.addReward(addRewardRequest);

        verify(rewardRepository, times(1)).save(rewardArgumentCaptor.capture());
        Reward savedReward = rewardArgumentCaptor.getValue();

        assertRewardCorrect(addRewardRequest, savedReward, null, "https://www.youtube.com/@");
    }

    @Test
    @DisplayName(value = "Add subscribe telegram reward")
    void addSubscribeTelegramReward() {
        AddRewardRequest addRewardRequest = RewardMocks.getRewardRequest(RewardType.SUBSCRIBE, IntegratedService.TELEGRAM);
        rewardService.addReward(addRewardRequest);
        verify(rewardRepository, times(1)).save(rewardArgumentCaptor.capture());
        Reward savedReward = rewardArgumentCaptor.getValue();

        assertRewardCorrect(addRewardRequest, savedReward, null, "https://t.me/");
    }

    @Test
    @DisplayName(value = "Receive reward")
    void receiveReward() {
        Account account = AccountMocks.getAccountMock(1L);
        Reward reward = RewardMocks.getRewardMock(2, RewardType.WATCH);

        DateRange dateRange = getDateRange();
        reward.setValidFrom(dateRange.getStart());
        reward.setEndsIn(dateRange.getEnd());

        when(accountService.getById(account.getId())).thenReturn(account);
        when(rewardRepository.findById(reward.getId())).thenReturn(Optional.of(reward));
        when(accountRewardsRepository.existsById(new AccountRewardKey(account.getId(), reward.getId()))).thenReturn(false);

        LocalDateTime clickTime = dateRange.getStart().plusMinutes(30);

        ReceiveRewardRequest request = new ReceiveRewardRequest(reward.getId(), clickTime);
        LocalDateTime currentDate = TimeUtil.getCurrentDateTime();
        rewardService.receiveReward(account.getId(), request);

        verify(accountRewardsRepository, times(1)).save(accountRewardsReceivedArgumentCaptor.capture());
        verify(accountService, times(1)).addCoins(account, reward.getRewardCoins());
        verify(accountService, times(1)).updateAccount(account);
        verify(accountService, times(1)).toDto(account);

        AccountRewardsReceived savedRecord = accountRewardsReceivedArgumentCaptor.getValue();
        assertEquals(account.getId(), savedRecord.getAccountId());
        assertEquals(reward.getId(), savedRecord.getRewardId());

        boolean dateEqual = savedRecord.getReceivedAt().isEqual(currentDate);
        boolean recordDateBetween = currentDate.isBefore(savedRecord.getReceivedAt())
                && currentDate.plusSeconds(5).isAfter(savedRecord.getReceivedAt());

        assertTrue(dateEqual || recordDateBetween);
    }

    @Test
    @DisplayName(value = "Receive same award twice")
    void receiveRewardTwice() {
        Account account = AccountMocks.getAccountMock(1L);
        Reward reward = RewardMocks.getRewardMock(1, RewardType.WATCH);

        DateRange dateRange = getDateRange();
        reward.setValidFrom(dateRange.getStart());
        reward.setEndsIn(dateRange.getEnd());

        when(accountService.getById(account.getId())).thenReturn(account);
        when(rewardRepository.findById(reward.getId())).thenReturn(Optional.of(reward));
        when(accountRewardsRepository.existsById(new AccountRewardKey(account.getId(), reward.getId()))).thenReturn(true);

        LocalDateTime clickTime = dateRange.getStart().plusMinutes(30);

        ReceiveRewardRequest request = new ReceiveRewardRequest(reward.getId(), clickTime);

        assertThrows(AppException.class, () -> rewardService.receiveReward(account.getId(), request));
    }

    @Test
    @DisplayName(value = "Receive reward before it has appeared")
    void receiveRewardBeforeItAppeared() {
        Account account = AccountMocks.getAccountMock(1L);
        Reward reward = RewardMocks.getRewardMock(1, RewardType.WATCH);

        DateRange dateRange = getDateRange();
        reward.setValidFrom(dateRange.getStart());
        reward.setEndsIn(dateRange.getEnd());

        when(accountService.getById(account.getId())).thenReturn(account);
        when(rewardRepository.findById(reward.getId())).thenReturn(Optional.of(reward));
        when(accountRewardsRepository.existsById(new AccountRewardKey(account.getId(), reward.getId()))).thenReturn(false);

        LocalDateTime clickTime = dateRange.getStart().minusSeconds(1);

        ReceiveRewardRequest request = new ReceiveRewardRequest(reward.getId(), clickTime);

        assertThrows(AppException.class, () -> rewardService.receiveReward(account.getId(), request));
    }

    @Test
    @DisplayName(value = "Receive reward after it has disappeared")
    void receiveRewardAfterItDisappeared() {
        Account account = AccountMocks.getAccountMock(1L);
        Reward reward = RewardMocks.getRewardMock(1, RewardType.WATCH);

        DateRange dateRange = getDateRange();
        reward.setValidFrom(dateRange.getStart());
        reward.setEndsIn(dateRange.getEnd());

        when(accountService.getById(account.getId())).thenReturn(account);
        when(rewardRepository.findById(reward.getId())).thenReturn(Optional.of(reward));
        when(accountRewardsRepository.existsById(new AccountRewardKey(account.getId(), reward.getId()))).thenReturn(false);

        LocalDateTime clickTime = dateRange.getStart().minusSeconds(1);

        ReceiveRewardRequest request = new ReceiveRewardRequest(reward.getId(), clickTime);

        assertThrows(AppException.class, () -> rewardService.receiveReward(account.getId(), request));
    }

    @Test
    @DisplayName(value = "Receive reward, click after current date")
    void receiveRewardClickAfterCurrentDate() {
        Account account = AccountMocks.getAccountMock(1L);
        Reward reward = RewardMocks.getRewardMock(1, RewardType.WATCH);

        DateRange dateRange = getDateRange();
        reward.setValidFrom(dateRange.getStart());
        reward.setEndsIn(dateRange.getEnd());
        LocalDateTime now = dateRange.getStart().plusHours(1);

        when(accountService.getById(account.getId())).thenReturn(account);
        when(rewardRepository.findById(reward.getId())).thenReturn(Optional.of(reward));
        when(accountRewardsRepository.existsById(new AccountRewardKey(account.getId(), reward.getId()))).thenReturn(false);

        LocalDateTime clickTime = now.plusMinutes(1);

        ReceiveRewardRequest request = new ReceiveRewardRequest(reward.getId(), clickTime);

        try (MockedStatic<TimeUtil> ignored = mockStatic(TimeUtil.class)) {
            mockTime(now, clickTime);
            assertThrows(AppException.class, () -> rewardService.receiveReward(account.getId(), request));
        }

    }

    @Test
    @DisplayName(value = "Receive reward, click no more than one day ago")
    void receiveRewardClickNoMoreThanOneDayAgo() {
        Account account = AccountMocks.getAccountMock(1L);
        Reward reward = RewardMocks.getRewardMock(1, RewardType.WATCH);

        DateRange dateRange = getDateRange();
        reward.setValidFrom(dateRange.getStart());
        reward.setEndsIn(dateRange.getEnd());
        LocalDateTime now = dateRange.getEnd().plusHours(24);

        when(accountService.getById(account.getId())).thenReturn(account);
        when(rewardRepository.findById(reward.getId())).thenReturn(Optional.of(reward));
        when(accountRewardsRepository.existsById(new AccountRewardKey(account.getId(), reward.getId()))).thenReturn(false);

        LocalDateTime clickTime = dateRange.getEnd().minusMinutes(1);

        ReceiveRewardRequest request = new ReceiveRewardRequest(reward.getId(), clickTime);

        try (MockedStatic<TimeUtil> ignored = mockStatic(TimeUtil.class)) {
            mockTime(now, clickTime);
            assertThrows(AppException.class, () -> rewardService.receiveReward(account.getId(), request));
        }
    }

    @Test
    @DisplayName(value = "Receive reward, without watching video")
    void receiveRewardWithoutWatchingVideo() {
        Account account = AccountMocks.getAccountMock(1L);
        Reward reward = RewardMocks.getRewardMock(1, RewardType.WATCH);
        reward.setDurationInSeconds(3 * 60);

        DateRange dateRange = getDateRange();
        reward.setValidFrom(dateRange.getStart());
        reward.setEndsIn(dateRange.getEnd());
        LocalDateTime now = dateRange.getStart().plusHours(1);

        when(accountService.getById(account.getId())).thenReturn(account);
        when(rewardRepository.findById(reward.getId())).thenReturn(Optional.of(reward));
        when(accountRewardsRepository.existsById(new AccountRewardKey(account.getId(), reward.getId()))).thenReturn(false);

        LocalDateTime clickTime = now.minusMinutes(1);

        ReceiveRewardRequest request = new ReceiveRewardRequest(reward.getId(), clickTime);

        try (MockedStatic<TimeUtil> ignored = mockStatic(TimeUtil.class)) {
            mockTime(now, clickTime);
            assertThrows(AppException.class, () -> rewardService.receiveReward(account.getId(), request));
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


    private void assertRewardCorrect(AddRewardRequest request, Reward savedReward, Integer durationInSeconds, String baseLink) {
        assertEquals(durationInSeconds, savedReward.getDurationInSeconds());
        assertEquals(request.getService(), savedReward.getIntegratedService());
        assertEquals(baseLink + request.getId(), savedReward.getLink());
        assertEquals(request.getRewardType(), savedReward.getType());
        assertEquals(request.getDateRange().getStart(), savedReward.getValidFrom());
        assertEquals(request.getDateRange().getEnd(), savedReward.getEndsIn());
        assertEquals(request.getRewardCoins(), savedReward.getRewardCoins());
        assertEquals(request.getRewardName(), savedReward.getName());
    }
}
