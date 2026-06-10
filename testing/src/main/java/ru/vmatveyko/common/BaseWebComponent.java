package ru.vmatveyko.common;

import java.time.Duration;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BaseWebComponent {

    public By locator;
    public WebElement we;
    private WebDriverWait wait;

    /**
     * @param locator By selenium
     */
    public BaseWebComponent(By locator) {
        this.locator = locator;
    }

    /**
     * @param driver selenium web driver
     * @param state state to wait for
     * @param waitTimeMillis wait time in milliseconds for web component desired state
     * @return selenium WebElement
     * @throws TimeoutException
     */
    public WebElement wait(WebDriver driver, Dictionary.States state, Long waitTimeMillis)
        throws TimeoutException {

        //if waitTimeMillis is null - Dictionary.DEFAULT_WAIT_TIME used for waiting web element
        Optional.ofNullable(waitTimeMillis).ifPresentOrElse(
            n -> wait = new WebDriverWait( driver, Duration.ofMillis(n) ),
            () -> wait = new WebDriverWait( driver, Duration.ofMillis(Dictionary.DEFAULT_WAIT_TIME) )
        );
        return switch (state) {
            case Visible   -> we = wait.until( ExpectedConditions.visibilityOfElementLocated(this.locator) );
            case Clickable -> we = wait.until( ExpectedConditions.elementToBeClickable(this.locator) );
            case Enable    -> we = driver.findElement(this.locator);
        };
    }

}
