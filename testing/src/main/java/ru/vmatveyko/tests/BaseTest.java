package ru.vmatveyko.tests;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

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

    protected void hardPause(long pauseMillis) {
        try {
            Thread.sleep(pauseMillis);
        } catch (InterruptedException e) {
            logger.error("Test hard sleep error: " + e.getMessage());
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
        container.getDriver().manage().deleteAllCookies();
        //container.getDriver().quit();

        if (Dictionary.CLEAR_COOKIES_AND_STORAGE) {
            JavascriptExecutor js = (JavascriptExecutor) container.getDriver();
            container.getDriver().manage().deleteAllCookies();
            js.executeScript("window.sessionStorage.clear()");
        }
        containerPool.release(container);
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
        //.addArguments("--window-size=1920,1080");

        //If the <suite> thread count is not explicitly defined in the TestNG XML or command line,
        // getSuite().getXmlSuite().getThreadCount() returns -1
        int parallelThreads = context.getSuite().getXmlSuite().getThreadCount();
        parallelThreads = (parallelThreads > 0) ? parallelThreads : 1;

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
                for (WebDriverContainer c : containerPool.getPool()) {
                    c.getContainer().stop();
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
    public WebDriverContainer initDriver() throws RuntimeException {
        WebDriverContainer driverContainer;
        try {
            driverContainer = containerPool.acquire();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            throw new RuntimeException("Could not acquire container for selenium web test");
        }

        driverContainer.getDriver().manage().window().maximize();
        return driverContainer;
    }

}
