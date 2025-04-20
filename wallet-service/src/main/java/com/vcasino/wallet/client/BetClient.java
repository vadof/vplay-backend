package com.vcasino.wallet.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "bet-service")
public interface BetClient extends Client {

}
