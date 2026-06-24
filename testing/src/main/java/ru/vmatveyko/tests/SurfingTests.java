package ru.vmatveyko.tests;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import io.qameta.allure.Description;
import io.qameta.allure.Flaky;
import ru.vmatveyko.common.Dictionary;
import ru.vmatveyko.common.WebDriverContainer;
import ru.vmatveyko.pages.DocumentationPage;
import ru.vmatveyko.pages.GitHubAuthPage;
import ru.vmatveyko.pages.SeleniumPage;

public class SurfingTests extends BaseTest {

    @Test
    @Description("Тест проверяет переход по ссылке с базовой страницы Selenium к документации")
    public void testSwitchToDoc() {

        logger.info("Starting switch to Documentation page test");
        //init
        SoftAssert softAssert = new SoftAssert();
        WebDriverContainer wdcontainer = initDriver();
        SeleniumPage selPage = new SeleniumPage(wdcontainer.getDriver(), getURL());
        //logic
        selPage.open();
        DocumentationPage docPage = selPage.switchToDocHeader(Dictionary.DEFAULT_WAIT_TIME);
        //checks
        softAssert.assertFalse(docPage.getIsInFailedState(), "Could not open Documentation Page for expected time");

        logger.info("Switch to Documentation page test finished");
        assertSoftAndReleaseContainer(wdcontainer, softAssert);
    }

    @Test(invocationCount=5)
    @Description("Тест проверяет открытие страницы с документацией Selenium")
    public void testOpenDoc() {
        logger.info("Starting opening Documentation page test");
        //init
        SoftAssert softAssert = new SoftAssert();
        WebDriverContainer wdcontainer = initDriver();
        DocumentationPage docPage = new DocumentationPage(wdcontainer.getDriver(), getURL() + "/documentation");
        //logic
        docPage.open().waitPage(Dictionary.DEFAULT_WAIT_TIME);
        //checks
        softAssert.assertFalse(docPage.getIsInFailedState(), "Could not open Documentation Page for expected time");

        logger.info("Opening Documentation page test finished");
        assertSoftAndReleaseContainer(wdcontainer, softAssert);
    }

    //@Test(dependsOnMethods = {"testSwitchToDoc"})
    @Test(invocationCount=4)
    @Parameters({"editDocLink"})
    @Description("Тест проверяет редактирование страницы с документацией Selenium")
    public void editDocumentationPage(String expectedUrl) {
        logger.info("Starting editing documentation page test");
        //init
        SoftAssert softAssert = new SoftAssert();
        WebDriverContainer wdcontainer = initDriver();
        DocumentationPage docPage = new DocumentationPage(wdcontainer.getDriver(), getURL() + "/documentation");
        //logic
        docPage.open();
        GitHubAuthPage gitHubPage = docPage.editingDocumentationPage(Dictionary.DEFAULT_WAIT_TIME);
        //checks
        softAssert.assertTrue(gitHubPage != null, "Could not open Edit Documentation Page for expected time");
        softAssert.assertEquals(gitHubPage.getURL(), expectedUrl);

        logger.info("Editing documentation page test finished");
        assertSoftAndReleaseContainer(wdcontainer, softAssert);
    }

    @Test(enabled = false)
    @Flaky
    @Description("Намеренно проваленный тест, ничего не проверяет")
    public void failedTest() {
        logger.info("Starting failed test");
        //init
        SoftAssert softAssert = new SoftAssert();
        WebDriverContainer wdcontainer = initDriver();
        DocumentationPage docPage = new DocumentationPage(wdcontainer.getDriver(), "https://www.google.com/");
        //logic
        docPage.open();
        softAssert.assertTrue(false);
        takeScreenShot(wdcontainer, "target/failedTest.png");

        logger.info("Intension fail test finished");
        assertSoftAndReleaseContainer(wdcontainer, softAssert);
    }

}
