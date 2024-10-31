package com.vcasino.clicker.dto.reward;

import com.vcasino.clicker.config.IntegratedService;
import com.vcasino.clicker.entity.enums.RewardType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RewardDto {
    Integer id;
    RewardType type;
    String name;
    String link;
    Integer durationInSeconds;
    IntegratedService integratedService;
    Integer rewardCoins;
    LocalDateTime endsIn;
    Boolean received;
}
