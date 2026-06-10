package ru.vmatveyko.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import ru.vmatveyko.common.BaseWebComponent;
import ru.vmatveyko.common.Dictionary;

public class DocumentationPage extends BasePage {

    private final BaseWebComponent docSidebar = new BaseWebComponent(
        By.className("td-sidebar-nav-active-item")
    );

    private final BaseWebComponent editPageLink = new BaseWebComponent(
        By.xpath("//a[text()=' Edit this page']")
    );

    /**
     * @param driver
     * @param url
     */
    public DocumentationPage(WebDriver driver, String url) {
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
            docSidebar.wait(driver, Dictionary.States.Visible, locatorWaitTimeMillis);
            editPageLink.wait(driver, Dictionary.States.Clickable, locatorWaitTimeMillis);
            allElementsWereLoaded = true;
        } catch(TimeoutException | NoSuchElementException e) {
            logger.error(e.getMessage());
            setIsInFailedState(true);
        }
    }

    /**
     * @param locatorWaitTimeMillis
     * @return GitHubAuthPage instance or null
     */
    public GitHubAuthPage editingDocumentationPage(long locatorWaitTimeMillis) {
        if (!allElementsWereLoaded)
            try {
                editPageLink.wait(driver, Dictionary.States.Clickable, locatorWaitTimeMillis).click();
            } catch(TimeoutException e) {
                logger.error(e.getMessage());
                setIsInFailedState(true);
                return null;
            }
        else editPageLink.we.click();
        return new GitHubAuthPage(driver, editPageLink.we.getAttribute("href"));
    }

}
