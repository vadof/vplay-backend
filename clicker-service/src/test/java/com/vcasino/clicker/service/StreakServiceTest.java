package com.vcasino.clicker.service;

import com.vcasino.clicker.dto.streak.DayReward;
import com.vcasino.clicker.dto.streak.StreakInfo;
import com.vcasino.clicker.dto.streak.StreakState;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.entity.Streak;
import com.vcasino.clicker.exception.AppException;
import com.vcasino.clicker.mock.AccountMocks;
import com.vcasino.clicker.mock.StreakMocks;
import com.vcasino.clicker.repository.StreakRepository;
import com.vcasino.clicker.utils.TimeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UNIT tests for {@link StreakService}
 */
@ExtendWith(MockitoExtension.class)
public class StreakServiceTest {

    @Mock
    private StreakRepository repository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private StreakService streakService;

    private Map<Integer, Integer> rewardsByDay;
    private List<DayReward> dayRewards;

    @BeforeEach
    void setUp() throws IllegalAccessException, NoSuchFieldException {
        dayRewards = StreakMocks.getDayRewards();
        rewardsByDay = new HashMap<>();
        dayRewards.forEach(r -> rewardsByDay.put(r.getDay(), r.getReward()));

        Field dayRewardsField = StreakService.class.getDeclaredField("dayRewards");
        dayRewardsField.setAccessible(true);
        dayRewardsField.set(streakService, dayRewards);

        Field rewardsByDayField = StreakService.class.getDeclaredField("rewardsByDay");
        rewardsByDayField.setAccessible(true);
        rewardsByDayField.set(streakService, rewardsByDay);
    }

    @Test
    @DisplayName("Get streak info")
    void getStreakInfo() {
        Account account = AccountMocks.getAccountMock(1L);
        Streak streak = StreakMocks.getStreakMock(account.getId());

        when(repository.findById(account.getId())).thenReturn(Optional.of(streak));
        StreakInfo streakInfo = streakService.getStreakInfo(account.getId());
        StreakState state = streakInfo.getState();

        assertEquals(1, state.getDay());
        assertTrue(state.getAvailable());

        assertEquals(dayRewards.size(), streakInfo.getRewardsByDays().size());
        for (int i = 0; i < dayRewards.size(); i++) {
            DayReward expectedReward = dayRewards.get(i);
            DayReward actualReward = streakInfo.getRewardsByDays().get(i);
            assertEquals(expectedReward.getDay(), actualReward.getDay());
            assertEquals(expectedReward.getReward(), actualReward.getReward());
        }
    }

    @Test
    @DisplayName("If streak doesn't exists it creates a new one")
    void getStreakInfoCreatesNew() {
        Account account = AccountMocks.getAccountMock(1L);
        Streak streak = StreakMocks.getStreakMock(account.getId());
        when(repository.findById(account.getId())).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(streak);

        StreakInfo streakInfo = streakService.getStreakInfo(account.getId());

        assertEquals(streak.getDay(), streakInfo.getState().getDay());
        assertTrue(streakInfo.getState().getAvailable());
    }

    @Test
    @DisplayName("Get state - last received day today")
    void getStreakReceivedToday() {
        Account account = AccountMocks.getAccountMock(1L);

        LocalDate today = TimeUtil.getCurrentDate();
        Streak streak = StreakMocks.getStreakMock(account.getId());
        streak.setLastReceivedDate(today);

        when(repository.findById(account.getId())).thenReturn(Optional.of(streak));
        StreakState state = streakService.getStreakInfo(account.getId()).getState();

        assertFalse(state.getAvailable());
    }

    @Test
    @DisplayName("Get state - last received day yesterday")
    void getStreakReceivedYesterday() {
        Account account = AccountMocks.getAccountMock(1L);

        LocalDate yesterday = TimeUtil.getCurrentDate().minusDays(1);
        Streak streak = StreakMocks.getStreakMock(account.getId());
        streak.setDay(1);
        streak.setLastReceivedDate(yesterday);

        when(repository.findById(account.getId())).thenReturn(Optional.of(streak));
        StreakState state = streakService.getStreakInfo(account.getId()).getState();

        assertTrue(state.getAvailable());
        assertEquals(2, state.getDay());
    }

    @Test
    @DisplayName("Get state - last received day week ago")
    void getStreakReceivedWeekAgo() {
        Account account = AccountMocks.getAccountMock(1L);

        LocalDate weekAgo = TimeUtil.getCurrentDate().minusDays(7);
        Streak streak = StreakMocks.getStreakMock(account.getId());
        streak.setDay(5);
        streak.setLastReceivedDate(weekAgo);

        when(repository.findById(account.getId())).thenReturn(Optional.of(streak));
        StreakState state = streakService.getStreakInfo(account.getId()).getState();

        assertTrue(state.getAvailable());
        assertEquals(1, state.getDay());
    }

    @Test
    @DisplayName("Get state - when day is 10 streak resets")
    void getStreakAfter10dayStreakResets() {
        Account account = AccountMocks.getAccountMock(1L);

        LocalDate yesterday = TimeUtil.getCurrentDate().minusDays(1);
        Streak streak = StreakMocks.getStreakMock(account.getId());
        streak.setDay(10);
        streak.setLastReceivedDate(yesterday);

        when(repository.findById(account.getId())).thenReturn(Optional.of(streak));
        StreakState state = streakService.getStreakInfo(account.getId()).getState();

        assertTrue(state.getAvailable());
        assertEquals(1, state.getDay());
    }

    @Test
    @DisplayName("Receive reward")
    void receiveReward() {
        Account account = AccountMocks.getAccountMock(1L);

        Streak streak = StreakMocks.getStreakMock(account.getId());

        when(repository.findById(account.getId())).thenReturn(Optional.of(streak));
        when(accountService.getById(account.getId())).thenReturn(account);

        streakService.receiveReward(account.getId());

        verify(repository, times(1)).save(streak);
        verify(accountService, times(1)).addCoins(account, rewardsByDay.get(streak.getDay()));
        verify(accountService, times(1)).updateAccount(account);
        verify(accountService, times(1)).toDto(account);

        assertEquals(1, streak.getDay());
        assertEquals(TimeUtil.getCurrentDate(), streak.getLastReceivedDate());
    }

    @Test
    @DisplayName("Receive reward correct amount of coins")
    void receiveRewardCorrectAmountOfCoins() {
        Account account = AccountMocks.getAccountMock(1L);

        for (int day = 1; day <= 10; day++) {
            Streak streak = StreakMocks.getStreakMock(account.getId());

            if (day != 1) {
                streak.setLastReceivedDate(TimeUtil.getCurrentDate().minusDays(1));
                streak.setDay(day - 1);
            } else {
                streak.setDay(day);
            }

            when(repository.findById(account.getId())).thenReturn(Optional.of(streak));
            when(accountService.getById(account.getId())).thenReturn(account);

            streakService.receiveReward(account.getId());
            verify(accountService, times(1)).addCoins(account, rewardsByDay.get(day));
        }
    }

    @Test
    @DisplayName("Receive reward twice in the same day throws an error")
    void receiveRewardTwiceInTheSameDay() {
        Account account = AccountMocks.getAccountMock(1L);
        Streak streak = StreakMocks.getStreakMock(account.getId());
        when(repository.findById(account.getId())).thenReturn(Optional.of(streak));
        when(accountService.getById(account.getId())).thenReturn(account);

        streakService.receiveReward(account.getId());
        assertThrows(AppException.class, () -> streakService.receiveReward(account.getId()));
    }

}
