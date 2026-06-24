package ru.vmatveyko.tests;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.asserts.SoftAssert;

import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import lombok.Getter;
import lombok.Setter;
import ru.vmatveyko.common.Dictionary;
import ru.vmatveyko.common.ResourcePool;
import ru.vmatveyko.common.WebDriverContainer;


public abstract class BaseTest {

    protected final Logger logger = LogManager.getLogger(getClass());

    @Getter @Setter
    protected String URL;
    private ChromeOptions chromeOptions;

    private ResourcePool<WebDriverContainer> containerPool;

    @Step
    protected void hardPause(long pauseMillis) {
        try {
            Thread.sleep(pauseMillis);
        } catch (InterruptedException e) {
            logger.error("Test hard sleep error: " + e.getMessage());
        }
    }

   /**
     * soft testng assertion execution
     *
     * @param WebDriverContainer (pair of selenium webdriver and testcontainer) instance
     * @param SoftAssert - TestNG SoftAssert
     */
    @Step
    protected void assertSoftAndReleaseContainer(WebDriverContainer wdcontainer, SoftAssert softAssert) {
        try {
            softAssert.assertAll();
        } catch(AssertionError e) {
            wdcontainer.setLastTestFailed(true);
            throw e;
        } finally {
            releaseContainer(wdcontainer);
        }
    }

    /**
     * Take screenshot
     *
     * @param WebDriverContainer (pair of selenium webdriver and testcontainer) instance
     * @param filePath - file path as String to save screenshot
     */
    @Step("Taking screenshot")
    @Attachment(value = "PageScreenshot", type = "image/png")
    protected byte[] takeScreenShot(WebDriverContainer wdcontainer, String filePath) {
        try {
            byte[] screenshotBytes = ((TakesScreenshot) wdcontainer.getDriver()).getScreenshotAs(OutputType.BYTES);
            try (OutputStream out = new BufferedOutputStream( new FileOutputStream(new File(filePath)) )
                ) {
                out.write(screenshotBytes);
                return screenshotBytes;
            }
        } catch (IOException ex) {
            logger.error("Could not take screenshot " + ex.getMessage());
            return null;
        }
    }

    /**
     * WebDriverContainer instance creation
     *
     * @return WebDriverContainer (pair of selenium webdriver and testcontainer) instance
     */
    private WebDriverContainer makeSeleniumContainerWithChrome() {
        //DockerImageName image = DockerImageName.parse("selenium/standalone-chrome:4.41.0");
        //container = new BrowserWebDriverContainer<>(image);
        @SuppressWarnings({ "resource", "deprecation" })
        BrowserWebDriverContainer<?> cont = new BrowserWebDriverContainer<>()
            .withReuse(Dictionary.HOLD_BROWSER_OPEN)
            //with reuse container option prevents same hash conflict
            .withEnv("INSTANCE_ID", UUID.randomUUID().toString())
            .withAccessToHost(true)
            .withCapabilities(chromeOptions)
            .withRecordingMode(Dictionary.CONTAINER_RECORDING_MODE, new File("build"));

        cont.start();
        return new WebDriverContainer(cont, new RemoteWebDriver(cont.getSeleniumAddress(), chromeOptions));
    }

    /**
     * release container back to resource pool
     *
     * @param container
     */
    public void releaseContainer(WebDriverContainer container) {
        //if last test case failed and keep browser mode is switched on
        if ( !(Dictionary.HOLD_BROWSER_OPEN && container.isLastTestFailed()) ) {
            container.getDriver().manage().deleteAllCookies();
            //container.getDriver().quit();

            if (Dictionary.CLEAR_COOKIES_AND_STORAGE) {
                try {
                    JavascriptExecutor js = (JavascriptExecutor) container.getDriver();
                    // Clear Local Storage
                    js.executeScript("window.localStorage.clear();");
                    // Clear Session Storage
                    js.executeScript("window.sessionStorage.clear();");
                    // Optional: Refresh the page to apply changes
                    container.getDriver().navigate().refresh();
                } catch (Exception e) {}
            }
            logger.info("releasing container for reuse...");
            containerPool.release(container);
        }
    }

    /**
     * @param url - base url
     * @param context - TestNG context to obtain thread count
     */
    @BeforeSuite
    @Parameters({"appUrl"})
    public void setUp(@Optional("https://default-url.com") String url, ITestContext context) {
        URL = url;
        chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--incognito");
        chromeOptions.addArguments("--ignore-certificate-errors");
        chromeOptions.addArguments("--no-sandbox");
        //.addArguments("--window-size=1920,1080");

        //If the <suite> thread count is not explicitly defined in the TestNG XML or command line,
        // getSuite().getXmlSuite().getThreadCount() returns -1
        final int parallelThreads = (context.getSuite().getXmlSuite().getThreadCount() > 0) ?
            context.getSuite().getXmlSuite().getThreadCount() : 1;

        containerPool = new ResourcePool<>(parallelThreads, () ->
            makeSeleniumContainerWithChrome()
        );

        logger.info("The profile setup process is completed");
    }

    /**
     * clear resource pool after test suite
     */
    @AfterSuite
    public void clear() {
        containerPool.removeResources(
            () -> {
                logger.warn("Stopping containers...");
                for (WebDriverContainer c : containerPool.getPool()) {
                    if ( !(Dictionary.HOLD_BROWSER_OPEN && c.isLastTestFailed()) ) {
                        c.getContainer().stop();
                    }
                }
            }
        );
    }

    /**
     * Borrow a resource from pool
     *
     * @return WebDriverContainer instance
     * @throws RuntimeException if could not acquire container
     */
    @Step
    public WebDriverContainer initDriver() throws RuntimeException {
        WebDriverContainer driverContainer;
        try {
            driverContainer = containerPool.acquire();
            if (driverContainer == null) throw new RuntimeException("Could not acquire container for selenium web test");
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            throw new RuntimeException("Could not acquire container for selenium web test");
        }

        driverContainer.getDriver().manage().window().maximize();
        return driverContainer;
    }

}
