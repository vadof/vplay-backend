package com.vcasino.bet.service.bet;

import com.vcasino.bet.dto.request.BetRequest;
import com.vcasino.bet.dto.response.BetDto;
import com.vcasino.bet.dto.response.BetResponse;
import com.vcasino.bet.dto.response.PaginatedResponse;
import com.vcasino.bet.entity.Bet;
import com.vcasino.bet.mapper.BetMapper;
import com.vcasino.bet.repository.BetRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Slf4j
public class BetService {

    private final BetProcessingService processingService;
    private final BetRepository betRepository;
    private final BetMapper betMapper;

    @Async("betTaskExecutor")
    @CircuitBreaker(name = "wallet")
    public CompletableFuture<BetResponse> addBetToProcessing(BetRequest request, Long userId) {
        return CompletableFuture.supplyAsync(() -> processingService.processBetPlace(request, userId),
                CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS));
    }

    public PaginatedResponse<BetDto> getUserBets(Long userId, Integer page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Bet> betPage = betRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<BetDto> data = betPage.getNumberOfElements() > 0 ? betMapper.toDtos(betPage.getContent())
                : Collections.emptyList();

        return new PaginatedResponse<>(page, betPage.getTotalPages(), data);
    }

}
