package ru.vmatveyko.tests;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.utility.DockerImageName;
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
    private FirefoxOptions firefoxOptions;
    @Getter @Setter
    protected List<String> testImages = new ArrayList<>();
    private final HashMap<String,ResourcePool<WebDriverContainer>> rpool = new HashMap<>();

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
     * @param wdcontainer (pair of selenium webdriver and testcontainer) instance
     * @param softAssert TestNG SoftAssert
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
     * soft testng assertion execution
     *
     * @param wdcList (pair of selenium webdriver and testcontainer) instance List
     * @param saList TestNG SoftAssert List
     */
    @Step
    protected void assertSoftAndReleaseContainers(
        List<WebDriverContainer> wdcList, List<SoftAssert> saList) {
            AssertionError exp = null;
            for (int i=0; i < wdcList.size(); i++) {
                try {
                    saList.get(i).assertAll();
                } catch(AssertionError e) {
                    if (exp == null) exp = new AssertionError();
                    wdcList.get(i).setLastTestFailed(true);
                    exp.addSuppressed(e);
                } finally {
                    releaseContainer(wdcList.get(i));
                }
            }

            if (exp != null) throw exp;

    }

    /**
     * Take screenshot
     *
     * @param wdcontainer (pair of selenium webdriver and testcontainer) instance
     * @param filePath file path as String to save screenshot
     * @param saveFile save screnshot file locally
     */
    @Step("Taking screenshot")
    @Attachment(value = "PageScreenshot", type = "image/png")
    protected byte[] takeScreenShot(WebDriverContainer wdcontainer, String filePath, boolean saveFile) {
        byte[] screenshotBytes = null;
        try {
            screenshotBytes = ((TakesScreenshot) wdcontainer.getDriver()).getScreenshotAs(OutputType.BYTES);
            if (saveFile)
                try (OutputStream out = new BufferedOutputStream( new FileOutputStream(new File(filePath)) )
                    ) {
                    out.write(screenshotBytes);
                }
        } catch (IOException ex) {
            logger.error("Could not take screenshot " + ex.getMessage());
        }
        return screenshotBytes;
    }

    /**
     * WebDriverContainer instance creation
     * @param capabilities webdriver capabilities
     * @param containerImg selenium image name (i.e. standalone-firefox:latest)
     * @return WebDriverContainer (pair of selenium webdriver and testcontainer) instance
     */
    private WebDriverContainer makeSeleniumContainer(Capabilities capabilities, String containerImg) {
        DockerImageName image = DockerImageName.parse(containerImg);
        @SuppressWarnings({ "resource", "deprecation" })
        BrowserWebDriverContainer<?> cont = new BrowserWebDriverContainer<>(image)
            .withReuse(Dictionary.HOLD_BROWSER_OPEN)
            //with reuse container option prevents same hash conflict
            .withEnv("INSTANCE_ID", UUID.randomUUID().toString())
            .withAccessToHost(true)
            .withCapabilities(capabilities)
            .withRecordingMode(Dictionary.CONTAINER_RECORDING_MODE, new File("build"));

        cont.start();
        return new WebDriverContainer(
            cont, new RemoteWebDriver(cont.getSeleniumAddress(), capabilities), containerImg);
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
            rpool.get(container.getContainerImage()).release(container);
            //containerPool.release(container);
        }
    }

    /**
     * @param url base url
     * @param context TestNG context to obtain thread count
     */
    @BeforeSuite
    @Parameters({"appUrl", "images"})
    public void setUp(@Optional("https://default-url.com") String url,
        String images, ITestContext context) {
        URL = url;

        List<String> imagesList = Arrays.asList(images.split(","));

        //If the <suite> thread count is not explicitly defined in the TestNG XML or command line,
        // getSuite().getXmlSuite().getThreadCount() returns -1
        final int parallelThreads = (context.getSuite().getXmlSuite().getThreadCount() > 0) ?
            context.getSuite().getXmlSuite().getThreadCount() : 1;

        ResourcePool<WebDriverContainer> containerPool = null;
        for (String img : imagesList) {
            if (img.toLowerCase().contains("chrome")) {
                chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--incognito");
                chromeOptions.addArguments("--ignore-certificate-errors");
                chromeOptions.addArguments("--no-sandbox");
                //.addArguments("--window-size=1920,1080");
                containerPool = new ResourcePool<>(parallelThreads, () ->
                    makeSeleniumContainer(chromeOptions, "selenium/"+img)
                );
            }
            else if (img.toLowerCase().contains("firefox")) {
                firefoxOptions = new FirefoxOptions();
                firefoxOptions.addArguments("--start-maximized");
                firefoxOptions.setAcceptInsecureCerts(true);
                containerPool = new ResourcePool<>(parallelThreads, () ->
                    makeSeleniumContainer(firefoxOptions, "selenium/"+img)
                );
            }
            rpool.put(img, containerPool);
            testImages.add(img);
        }


        logger.info("The profile setup process is completed");
    }

    /**
     * clear resource pool after test suite
     */
    @AfterSuite
    public void clear() {
        rpool.forEach((key, containerPool) -> {
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
        });
    }

    /**
     * Borrow a resource from pool
     *
     * @param image container image
     * @return WebDriverContainer instance
     * @throws RuntimeException if could not acquire container
     */
    @Step
    public WebDriverContainer initDriver(String image) throws RuntimeException {
        WebDriverContainer driverContainer;
        try {
            driverContainer = rpool.get(image).acquire();
            if (driverContainer == null) throw new RuntimeException("Could not acquire container for selenium web test");
            else driverContainer.setContainerImage(image);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            throw new RuntimeException("Could not acquire container for selenium web test");
        }

        driverContainer.getDriver().manage().window().maximize();
        return driverContainer;
    }

}
