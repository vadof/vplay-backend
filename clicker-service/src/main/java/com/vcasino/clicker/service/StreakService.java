package com.vcasino.clicker.service;

import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.dto.streak.DailyReward;
import com.vcasino.clicker.dto.streak.StreakInfo;
import com.vcasino.clicker.dto.streak.StreakState;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.entity.Streak;
import com.vcasino.clicker.exception.AppException;
import com.vcasino.clicker.repository.StreakRepository;
import com.vcasino.clicker.utils.TimeUtil;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class StreakService {

    private List<DailyReward> dailyRewards;
    private final Map<Integer, Integer> rewardsByDay = new HashMap<>();
    private final StreakRepository repository;
    private final AccountService accountService;

    public StreakInfo getStreakInfo(Long accountId) {
        Streak streak = getStreak(accountId);
        StreakState streakState = getStreakState(streak);
        return new StreakInfo(dailyRewards, streakState);
    }

    public AccountDto receiveReward(Long accountId) {
        Streak streak = getStreak(accountId);
        StreakState streakState = getStreakState(streak);

        if (!streakState.getAvailable()) {
            log.warn("Account#{} is attempting to claim the same reward twice", accountId);
            throw new AppException("The reward has already been received", HttpStatus.BAD_REQUEST);
        }

        Account account = accountService.getById(streak.getAccountId());
        accountService.addCoins(account, rewardsByDay.get(streakState.getDay()));
        accountService.updateAccount(account);

        streak.setDay(streakState.getDay());
        streak.setLastReceivedDate(TimeUtil.getCurrentDate());
        repository.save(streak);

        log.debug("Account#{} took daily reward #{}", accountId, streakState.getDay());

        return accountService.toDto(account);
    }

    private StreakState getStreakState(Streak streak) {
        if (streak.getLastReceivedDate() == null) {
            return new StreakState(1, true);
        } else if (TimeUtil.isToday(streak.getLastReceivedDate())) {
            return new StreakState(streak.getDay(), false);
        } else if (TimeUtil.isYesterday(streak.getLastReceivedDate())) {
            return new StreakState(streak.getDay() % 10 + 1, true);
        } else {
            return new StreakState(1, true);
        }
    }

    private Streak getStreak(Long accountId) {
        return repository.findById(accountId).orElseGet(() -> startNewStreak(accountId));
    }

    private Streak startNewStreak(Long accountId) {
        return repository.save(new Streak(accountId, 1, null));
    }

    @PostConstruct
    private void initDailyRewards() {
        dailyRewards = new ArrayList<>();
        dailyRewards.add(new DailyReward(1, 500));
        dailyRewards.add(new DailyReward(2, 1000));
        dailyRewards.add(new DailyReward(3, 2500));
        dailyRewards.add(new DailyReward(4, 5000));
        dailyRewards.add(new DailyReward(5, 15000));
        dailyRewards.add(new DailyReward(6, 25000));
        dailyRewards.add(new DailyReward(7, 100000));
        dailyRewards.add(new DailyReward(8, 250000));
        dailyRewards.add(new DailyReward(9, 500000));
        dailyRewards.add(new DailyReward(10, 1000000));
        dailyRewards.forEach(r -> rewardsByDay.put(r.getDay(), r.getReward()));
    }
}
