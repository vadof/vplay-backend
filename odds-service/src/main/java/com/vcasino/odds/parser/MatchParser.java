package com.vcasino.odds.parser;

import com.vcasino.odds.exception.ParserException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public abstract class MatchParser {

    private static final Logger log = LoggerFactory.getLogger(MatchParser.class);
    protected WebDriver mainDriver;
    protected final String matchPage;

    public MatchParser(String matchPage) throws ParserException {
        this.matchPage = matchPage;

        Optional<WebDriver> webDriverOptional = startDriver(matchPage);
        if (webDriverOptional.isEmpty()) {
            throw new ParserException("Unable to start driver for " + matchPage);
        }

        this.mainDriver = webDriverOptional.get();
        log.info("Driver for {} initialized", matchPage);
    }

    protected Optional<WebDriver> startDriver(String page) {
        ChromeOptions options = new ChromeOptions();

        int attempts = 3;
        while (attempts > 0) {
            ChromeDriver driver = new ChromeDriver(options);
            boolean success = false;
            try {
                driver.get(page);
                Thread.sleep(10000);
                success = true;
                return Optional.of(driver);
            } catch (Exception e) {
                attempts--;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
                log.warn("Exception starting driver for {}, attempts remains {}. Reason -> {}", page, attempts, e.getMessage());
            } finally {
                if (!success && driver != null) {
                    driver.close();
                }
            }
        }

        return Optional.empty();
    }

    public void close() {
        if (mainDriver != null) {
            mainDriver.close();
        }
    }

    public abstract void updateMatchPage(boolean updateWithPageRefresh) throws ParserException;
}
