package com.vcasino.clicker.mock;

import com.vcasino.clicker.dto.streak.DailyReward;
import com.vcasino.clicker.entity.Streak;

import java.util.List;

public class StreakMocks {

    public static Streak getStreakMock(Long accountId) {
        return new Streak(accountId, 1, null);
    }

    public static List<DailyReward> getDailyRewards() {
        return List.of(
                new DailyReward(1, 500),
                new DailyReward(2, 1000),
                new DailyReward(3, 2500),
                new DailyReward(4, 5000),
                new DailyReward(5, 15000),
                new DailyReward(6, 25000),
                new DailyReward(7, 100000),
                new DailyReward(8, 250000),
                new DailyReward(9, 500000),
                new DailyReward(10, 1000000)
        );
    }

}
