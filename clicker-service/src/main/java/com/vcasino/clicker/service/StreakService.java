package com.vcasino.clicker.service;

import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.dto.streak.DayReward;
import com.vcasino.clicker.dto.streak.StreakInfo;
import com.vcasino.clicker.dto.streak.StreakState;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.entity.Streak;
import com.vcasino.clicker.exception.AppException;
import com.vcasino.clicker.repository.StreakRepository;
import com.vcasino.clicker.utils.TimeUtil;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class StreakService {

    private List<DayReward> dayRewards;
    private final Map<Integer, Integer> rewardsByDay = new HashMap<>();
    private final StreakRepository repository;
    private final AccountService accountService;

    public StreakInfo getStreakInfo(Long accountId) {
        Streak streak = getStreak(accountId);
        StreakState streakState = getStreakState(streak);
        return new StreakInfo(dayRewards, streakState);
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

        log.info("Account#{} took daily reward #{}", accountId, streakState.getDay());

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
    private void initRewards() {
        dayRewards = new ArrayList<>();
        dayRewards.add(new DayReward(1, 500));
        dayRewards.add(new DayReward(2, 1000));
        dayRewards.add(new DayReward(3, 2500));
        dayRewards.add(new DayReward(4, 5000));
        dayRewards.add(new DayReward(5, 15000));
        dayRewards.add(new DayReward(6, 25000));
        dayRewards.add(new DayReward(7, 10000));
        dayRewards.add(new DayReward(8, 250000));
        dayRewards.add(new DayReward(9, 500000));
        dayRewards.add(new DayReward(10, 1000000));
        dayRewards.forEach(r -> rewardsByDay.put(r.getDay(), r.getReward()));
    }
}
