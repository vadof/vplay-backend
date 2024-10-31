package com.vcasino.clicker.mock;

import com.vcasino.clicker.config.IntegratedService;
import com.vcasino.clicker.dto.DateRange;
import com.vcasino.clicker.dto.reward.AddRewardRequest;
import com.vcasino.clicker.entity.Reward;
import com.vcasino.clicker.entity.enums.RewardType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RewardMocks {
    public static Reward getRewardMock(Integer id, RewardType rewardType) {
        return Reward.builder()
                .id(id)
                .type(rewardType)
                .name("Take reward")
                .link("https://youtube.com/watch?v" + id)
                .integratedService(IntegratedService.YOUTUBE)
                .durationInSeconds(rewardType == RewardType.WATCH ? 10 : 0)
                .rewardCoins(1000)
                .validFrom(LocalDateTime.of(2024, 1, 1, 0, 0))
                .endsIn(null)
                .build();
    }

    public static List<Reward> getRewardMocks(int n) {
        List<Reward> rewards = new ArrayList<>();
        for (int i = 1; i < n + 1; i++) {
            rewards.add(getRewardMock(i, i % 2 == 0 ? RewardType.SUBSCRIBE : RewardType.WATCH));
        }
        return rewards;
    }

    public static AddRewardRequest getRewardRequest(RewardType type, IntegratedService service) {
        return AddRewardRequest.builder()
                .id("id")
                .rewardType(type)
                .service(service)
                .rewardCoins(100)
                .rewardName("name")
                .dateRange(new DateRange(LocalDateTime.of(2024, 1, 1, 0, 0), null))
                .build();
    }
}
