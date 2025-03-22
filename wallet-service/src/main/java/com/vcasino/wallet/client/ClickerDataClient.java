package com.vcasino.wallet.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "clicker-data-service")
public interface ClickerDataClient extends Client {

}
