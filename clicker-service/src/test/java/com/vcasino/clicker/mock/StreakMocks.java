package com.vcasino.clicker.mock;

import com.vcasino.clicker.dto.streak.DayReward;
import com.vcasino.clicker.entity.Streak;

import java.util.List;

public class StreakMocks {

    public static Streak getStreakMock(Long accountId) {
        return new Streak(accountId, 1, null);
    }

    public static List<DayReward> getDayRewards() {
        return List.of(
                new DayReward(1, 500),
                new DayReward(2, 1000),
                new DayReward(3, 2500),
                new DayReward(4, 5000),
                new DayReward(5, 15000),
                new DayReward(6, 25000),
                new DayReward(7, 100000),
                new DayReward(8, 250000),
                new DayReward(9, 500000),
                new DayReward(10, 1000000)
        );
    }

}
