package com.vcasino.odds.parser;

import com.vcasino.odds.exception.ParserException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.util.Optional;

public abstract class MatchParser {

//    protected final WebDriver driver;

    public MatchParser(String matchPage) throws ParserException {
//        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver\\chromedriver.exe");

        System.out.println("Initialized driver with match page " + matchPage);
//        ChromeOptions options = new ChromeOptions();
//        this.driver = new ChromeDriver(options);

        try {
//            driver.get(matchPage);
//            Thread.sleep(5000);
            updateMatchPage(matchPage.equals("1") ? 55 : 1, Long.parseLong(matchPage));
        } catch (Exception e) {
//            this.driver = null;
            throw new ParserException("Exception happened during driver initialization", e);
        } finally {
//            driver.close();
        }
    }

    protected Optional<WebDriver> startDriver(String page) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addExtensions(new File("C:\\chromedriver\\FIHNJJCCIAJHDOJFNBDDDFAOKNHALNJA_3_5_1_0.crx"));
        ChromeDriver driver = new ChromeDriver(options);
        try {
            driver.get(page);
            Thread.sleep(5000);
        } catch (Exception e) {
            return Optional.empty();
        }

        return Optional.of(driver);
    }

    // TODO uncomment
    public void close() {
//        if (driver != null) {
//            driver.close();
//        }
    }

    public abstract void updateMatchPage(int saveCounter, Long matchId) throws ParserException;
}
