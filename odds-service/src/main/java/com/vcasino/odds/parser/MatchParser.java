package com.vcasino.odds.parser;

import com.vcasino.odds.exception.ParserException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;

public abstract class MatchParser {

    private static final Logger log = LoggerFactory.getLogger(MatchParser.class);
    protected WebDriver mainDriver;
    protected final String matchPage;
    protected final URL seleniumUrl;

    public MatchParser(String matchPage, URL seleniumUrl) throws ParserException {
        this.matchPage = matchPage;
        this.seleniumUrl = seleniumUrl;

        Optional<WebDriver> webDriverOptional = startDriver(matchPage);
        if (webDriverOptional.isEmpty()) {
            throw new ParserException("Unable to start driver for " + matchPage);
        }

        this.mainDriver = webDriverOptional.get();
        log.info("Driver for {} initialized", matchPage);
    }

    protected Optional<WebDriver> startDriver(String page) {
        ChromeOptions options = getOptions();

        int attempts = 3;
        while (attempts > 0) {
            WebDriver driver = new RemoteWebDriver(seleniumUrl, options);
            ((JavascriptExecutor) driver).executeScript(
                    "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})"
            );

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

    private ChromeOptions getOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        return options;
    }

    public abstract void updateMatchPage(boolean updateWithPageRefresh) throws ParserException;
}
