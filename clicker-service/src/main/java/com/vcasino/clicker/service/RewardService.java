package com.vcasino.clicker.service;

import com.vcasino.clicker.config.IntegratedService;
import com.vcasino.clicker.dto.AccountDto;
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
import com.vcasino.clicker.repository.AccountRewardsReceivedRepository;
import com.vcasino.clicker.repository.RewardRepository;
import com.vcasino.clicker.service.video.YoutubeService;
import com.vcasino.clicker.utils.TimeUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class RewardService {

    private final YoutubeService youtubeService;
    private final RewardRepository rewardRepository;
    private final AccountRewardsReceivedRepository accountRewardsRepository;
    private final RewardMapper mapper;
    private final AccountService accountService;

    public List<RewardDto> getRewards(Long accountId) {
        List<Reward> availableRewards = getAvailableRewards();
        Set<Integer> receivedRewardsIdsByUser = accountRewardsRepository.findRewardIdsByAccountId(accountId);

        List<RewardDto> rewardDtos = mapper.toDtos(availableRewards);
        rewardDtos.forEach(r -> r.setReceived(receivedRewardsIdsByUser.contains(r.getId())));
        return rewardDtos;
    }

    private List<Reward> getAvailableRewards() {
        return rewardRepository.findAllInInterval(TimeUtil.getCurrentDateTime());
    }

    public Map<RewardType, List<IntegratedService>> getSupportedServicesByRewardType() {
        Map<RewardType, List<IntegratedService>> map = new HashMap<>();
        for (RewardType rewardType : RewardType.values()) {
            for (IntegratedService service : IntegratedService.values()) {
                if (rewardType.serviceIsSupported(service)) {
                    map.computeIfAbsent(rewardType, r -> new ArrayList<>()).add(service);
                }
            }
        }
        return map;
    }

    public VideoInfo getVideoInfo(String videoId, IntegratedService integratedService) {
        return switch (integratedService) {
            case YOUTUBE -> youtubeService.getVideoInfo(videoId);
            default -> throw new AppException(integratedService.value() + " doesn't support watching", HttpStatus.BAD_REQUEST);
        };
    }

    public void addReward(AddRewardRequest request) {
        Reward reward = switch (request.getRewardType()) {
            case WATCH -> addWatchReward(request);
            case SUBSCRIBE -> addSubscribeReward(request);
        };

        rewardRepository.save(reward);

        log.info("Reward#{} saved in the database", reward.getId());
    }

    private Reward addWatchReward(AddRewardRequest request) {
        Reward reward = new Reward();
        setCommonFields(reward, request);
        reward.setLink(getWatchLink(request.getId(), request.getService()));
        reward.setDurationInSeconds(getVideoInfo(request.getId(), request.getService()).getDuration().toSecondOfDay());
        return reward;
    }

    private String getWatchLink(String id, IntegratedService integratedService) {
        return switch (integratedService) {
            case YOUTUBE -> "https://www.youtube.com/watch?v=" + id;
            default -> throw new AppException(integratedService.value() + " doesn't support watching", HttpStatus.BAD_REQUEST);
        };
    }

    private Reward addSubscribeReward(AddRewardRequest request) {
        Reward reward = new Reward();
        setCommonFields(reward, request);
        reward.setLink(getSubscribeLink(request.getId(), request.getService()));
        reward.setDurationInSeconds(null);
        return reward;
    }

    private void setCommonFields(Reward reward, AddRewardRequest request) {
        reward.setType(request.getRewardType());
        reward.setName(request.getRewardName());
        reward.setIntegratedService(request.getService());
        reward.setRewardCoins(request.getRewardCoins());
        reward.setValidFrom(request.getDateRange().getStart());
        reward.setEndsIn(request.getDateRange().getEnd());
    }

    private String getSubscribeLink(String id, IntegratedService integratedService) {
        return switch (integratedService) {
            case YOUTUBE -> "https://www.youtube.com/@" + id;
            case TELEGRAM -> "https://t.me/" + id;
        };
    }

    @Transactional
    public AccountDto receiveReward(Long accountId, ReceiveRewardRequest rewardRequest) {
        Reward reward = rewardRepository.findById(rewardRequest.getRewardId())
                .orElseThrow(() -> new AppException(null, HttpStatus.FORBIDDEN));

        Account account = accountService.getById(accountId);

        boolean rewardAlreadyReceived = accountRewardsRepository.existsById(new AccountRewardKey(accountId, reward.getId()));
        if (rewardAlreadyReceived) {
            throw new AppException("Reward already received", HttpStatus.BAD_REQUEST);
        }

        validateClickTime(rewardRequest.getClickTime(), reward);

        AccountRewardsReceived rewardReceived = AccountRewardsReceived.builder()
                .accountId(accountId)
                .rewardId(reward.getId())
                .receivedAt(TimeUtil.getCurrentDateTime())
                .build();

        accountRewardsRepository.save(rewardReceived);
        accountService.addCoins(account, reward.getRewardCoins());
        accountService.updateAccount(account);

        log.info("Account#{} received Reward#{}", accountId, reward.getId());

        return accountService.toDto(account);
    }

    private void validateClickTime(LocalDateTime clickTime, Reward reward) {
        LocalDateTime now = TimeUtil.getCurrentDateTime();

        int durationInSeconds = reward.getDurationInSeconds() == null ? 0 : 120;
        int rewardMarginInSeconds = durationInSeconds + 120;

        boolean clickAfterRewardAppeared = clickTime.isAfter(reward.getValidFrom());
        boolean clickBeforeRewardExpired = reward.getEndsIn() == null ||
                clickTime.isBefore(reward.getEndsIn().plusSeconds(rewardMarginInSeconds));
        boolean clickBeforeCurrentDate = clickTime.isBefore(now);
        boolean clickWasNoMoreThanOneDayAgo = clickTime.isAfter(now.minusDays(1));

        if (clickAfterRewardAppeared && clickBeforeRewardExpired
                && clickBeforeCurrentDate && clickWasNoMoreThanOneDayAgo) {
            long secondsSinceClick = TimeUtil.getDifferenceInSeconds(clickTime, now);
            switch (reward.getType()) {
                case SUBSCRIBE -> {}
                case WATCH -> {
                    if (secondsSinceClick < reward.getDurationInSeconds()) {
                        throw new AppException("Video not watched", HttpStatus.BAD_REQUEST);
                    }
                }
                default -> log.debug("Please add logic for a new RewardType");
            }
        } else {
            throw new AppException("Reward expired", HttpStatus.BAD_REQUEST);
        }
    }
}
