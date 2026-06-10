package ru.vmatveyko.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import ru.vmatveyko.common.BaseWebComponent;
import ru.vmatveyko.common.Dictionary;

public class SeleniumPage extends BasePage {

    private final BaseWebComponent docHeaderLink = new BaseWebComponent(
        By.xpath("//a/span[text()='Documentation']/..")
    );

    /**
     * @param driver
     * @param url
     */
    public SeleniumPage(WebDriver driver, String url) {
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
            docHeaderLink.wait(driver, Dictionary.States.Clickable, locatorWaitTimeMillis);
            allElementsWereLoaded = true;
        } catch(TimeoutException | NoSuchElementException e) {
            logger.error(e.getMessage());
            setIsInFailedState(true);
        }
    }

    /**
     * @param locatorWaitTimeMillis
     * @return DocumentationPage instance or null
     */
    public DocumentationPage switchToDocHeader(long locatorWaitTimeMillis) {
        if (!allElementsWereLoaded)
            try {
                docHeaderLink.wait(driver, Dictionary.States.Clickable, locatorWaitTimeMillis).click();
            } catch(TimeoutException | NoSuchElementException e) {
                logger.error(e.getMessage());
                setIsInFailedState(true);
                return null;
            }
        else docHeaderLink.we.click();
        return new DocumentationPage(driver, getURL() + "/documentation");
    }

}
