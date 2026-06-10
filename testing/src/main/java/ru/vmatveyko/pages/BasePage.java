package ru.vmatveyko.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import lombok.Getter;
import lombok.Setter;

public abstract class BasePage {

    protected Boolean allElementsWereLoaded = false;
    @Getter @Setter
    protected Boolean isInFailedState = false;
    @Getter @Setter
    protected WebDriver driver;
    @Getter @Setter
    protected String URL;
    protected final Logger logger = LogManager.getLogger(getClass());

    /**
     * waits for {locatorWaitTimeMillis} all locators to be in ready to use state
     *
     * @param locatorWaitTimeMillis
     */
    public abstract void waitPage(long locatorWaitTimeMillis);

    /**
     * @param driver
     * @param url
     */
    public BasePage(WebDriver driver, String url) {
        this.driver = driver;
        this.URL = url;
    }

    /**
     * @param driver
     */
    public BasePage(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * @param url to open
     * @return BasePage instance
     */
    public BasePage open(String url) {
        driver.get(url);
        return this;
    }

    /**
     * @return BasePage instance
     */
    public BasePage open() {
        driver.get(this.URL);
        return this;
    }

}
