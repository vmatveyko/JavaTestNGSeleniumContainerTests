package ru.vmatveyko.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import ru.vmatveyko.common.BaseWebComponent;
import ru.vmatveyko.common.Dictionary;


public class GitHubAuthPage extends BasePage {

    private final BaseWebComponent gitHubAuthFormHeader = new BaseWebComponent(
        By.xpath("//h1[text()='Sign in to GitHub']")
    );

    /**
     * @param driver
     * @param url
     */
    public GitHubAuthPage(WebDriver driver, String url) {
        super(driver, url);
    }

    /**
     * waits for {locatorWaitTimeMillis} all locators to be in ready to use state
     *
     * @param locatorWaitTimeMillis
     */
    @Override
    public void waitPage(long locatorWaitTimeMillis) {
        logger.info("waiting for all necessary page locators");
        try {
            gitHubAuthFormHeader.wait(driver, Dictionary.States.Visible, locatorWaitTimeMillis);
            allElementsWereLoaded = true;
        } catch(TimeoutException | NoSuchElementException e) {
            logger.error(e.getMessage());
            setIsInFailedState(true);
        }
    }

}
