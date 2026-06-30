package ru.vmatveyko.tests;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Flaky;
import io.qameta.allure.testng.Tag;
import ru.vmatveyko.common.Dictionary;
import ru.vmatveyko.common.WebDriverContainer;
import ru.vmatveyko.pages.DocumentationPage;
import ru.vmatveyko.pages.GitHubAuthPage;
import ru.vmatveyko.pages.SeleniumPage;

public class SurfingTests extends BaseTest {

    @Test
    @Parameters({"browsers"})
    @Tag("WEB")
    @Description("Тест проверяет переход по ссылке с базовой страницы Selenium к документации")
    public void testSwitchToDoc(String browsers) {
        //init
        List<String> browsersList = Arrays.asList(browsers.split(","));
        List<WebDriverContainer> wdList = new ArrayList<>();
        List<SoftAssert> softAssertList = new ArrayList<>();

        for (String browser : browsersList) {
            String image = getTestImages().stream().filter(name -> name.toLowerCase().contains(browser))
            .findFirst().orElseThrow(() -> new RuntimeException("Missing image"));
            wdList.add(initDriver(image));
        }

        logger.info("Starting switch to Documentation page test");
        for (WebDriverContainer wdcontainer : wdList) {
            SoftAssert softAssert = new SoftAssert();
            SeleniumPage selPage = new SeleniumPage(wdcontainer.getDriver(), getURL());
            //logic
            selPage.open();
            DocumentationPage docPage = selPage.switchToDocHeader(Dictionary.DEFAULT_WAIT_TIME);
            //checks
            softAssert.assertFalse(docPage.getIsInFailedState(), "Could not open Documentation Page for expected time");
            //if (wdcontainer.getContainerImage().contains("firefox")) softAssert.assertFalse(true);
            softAssertList.add(softAssert);
        }

        assertSoftAndReleaseContainers(wdList, softAssertList);

        logger.info("Switch to Documentation page test finished");
    }

    @Test(invocationCount=5)
    @Parameters({"browsers"})
    @Tag("WEB")
    @Description("Тест проверяет открытие страницы с документацией Selenium")
    public void testOpenDoc(String browsers) {
        //init
        List<String> browsersList = Arrays.asList(browsers.split(","));
        List<WebDriverContainer> wdList = new ArrayList<>();
        List<SoftAssert> softAssertList = new ArrayList<>();

        for (String browser : browsersList) {
            String image = getTestImages().stream().filter(name -> name.toLowerCase().contains(browser))
            .findFirst().orElseThrow(() -> new RuntimeException("Missing image"));
            wdList.add(initDriver(image));
        }

        logger.info("Starting opening Documentation page test");
        for (WebDriverContainer wdcontainer : wdList) {
            SoftAssert softAssert = new SoftAssert();
            DocumentationPage docPage = new DocumentationPage(wdcontainer.getDriver(), getURL() + "/documentation");
            //logic
            docPage.open().waitPage(Dictionary.DEFAULT_WAIT_TIME);
            //checks
            softAssert.assertFalse(docPage.getIsInFailedState(), "Could not open Documentation Page for expected time");
            softAssertList.add(softAssert);
        }

        assertSoftAndReleaseContainers(wdList, softAssertList);
        logger.info("Opening Documentation page test finished");
    }

    //@Test(dependsOnMethods = {"testSwitchToDoc"})
    @Test(invocationCount=4)
    @Parameters({"editDocLink", "browsers"})
    @Description("Тест проверяет редактирование страницы с документацией Selenium")
    public void editDocumentationPage(String expectedUrl, String browsers) {
        //init
        List<String> browsersList = Arrays.asList(browsers.split(","));
        List<WebDriverContainer> wdList = new ArrayList<>();
        List<SoftAssert> softAssertList = new ArrayList<>();

        for (String browser : browsersList) {
            String image = getTestImages().stream().filter(name -> name.toLowerCase().contains(browser))
            .findFirst().orElseThrow(() -> new RuntimeException("Missing image"));
            wdList.add(initDriver(image));
        }

        logger.info("Starting opening Documentation page test");
        for (WebDriverContainer wdcontainer : wdList) {
            SoftAssert softAssert = new SoftAssert();
            DocumentationPage docPage = new DocumentationPage(wdcontainer.getDriver(), getURL() + "/documentation");
            //logic
            docPage.open();
            GitHubAuthPage gitHubPage = docPage.editingDocumentationPage(Dictionary.DEFAULT_WAIT_TIME);
            //checks
            softAssert.assertTrue(gitHubPage != null, "Could not open Edit Documentation Page for expected time");
            softAssert.assertEquals(gitHubPage.getURL(), expectedUrl);
            softAssertList.add(softAssert);
        }

        assertSoftAndReleaseContainers(wdList, softAssertList);
        logger.info("Editing documentation page test finished");
    }

    @Test(enabled = true)
    @Parameters({"browsers"})
    @Tag("WEB")
    @Flaky
    @Description("Намеренно проваленный тест, ничего не проверяет")
    public void failedTest(String browsers) {
        //init
        List<String> browsersList = Arrays.asList(browsers.split(","));
        List<WebDriverContainer> wdList = new ArrayList<>();
        List<SoftAssert> softAssertList = new ArrayList<>();
        logger.info("Starting failed test");

        for (String browser : browsersList) {
            String image = getTestImages().stream().filter(name -> name.toLowerCase().contains(browser))
            .findFirst().orElseThrow(() -> new RuntimeException("Missing image"));
            wdList.add(initDriver(image));
        }

        logger.info("Starting opening Documentation page test");
        for (WebDriverContainer wdcontainer : wdList) {
            SoftAssert softAssert = new SoftAssert();
            DocumentationPage docPage = new DocumentationPage(wdcontainer.getDriver(), "https://www.google.com/");
            //logic
            docPage.open();
            softAssert.assertTrue(false);
            Allure.attachment("failedTest.png", new ByteArrayInputStream(
                takeScreenShot(wdcontainer, "target/failedTest.png", false))
            );
            softAssertList.add(softAssert);
        }

        assertSoftAndReleaseContainers(wdList, softAssertList);
        logger.info("Intension fail test finished");
    }

}
