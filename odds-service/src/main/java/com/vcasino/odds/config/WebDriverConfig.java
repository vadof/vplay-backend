package com.vcasino.odds.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WebDriverConfig {

    @Value("${chrome.driver.path}")
    private String chromeDriverPath;

    @PostConstruct
    public void setChromeDriverPath() {
        if (Strings.isBlank(chromeDriverPath)) {
            log.error("Chrome Driver not found");
            System.exit(1);
        }
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);
    }

}
