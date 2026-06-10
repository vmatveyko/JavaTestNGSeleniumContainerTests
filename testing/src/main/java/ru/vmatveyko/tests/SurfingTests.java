package ru.vmatveyko.tests;

import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import ru.vmatveyko.common.Dictionary;
import ru.vmatveyko.common.WebDriverContainer;
import ru.vmatveyko.pages.DocumentationPage;
import ru.vmatveyko.pages.GitHubAuthPage;
import ru.vmatveyko.pages.SeleniumPage;

public class SurfingTests extends BaseTest {

    @Test
    public void testSwitchToDoc() {
        logger.info("Starting switch to Documentation page test");
        //init
        WebDriverContainer wdcontainer = initDriver();
        SeleniumPage selPage = new SeleniumPage(wdcontainer.getDriver(), getURL());
        //logic
        selPage.open();
        DocumentationPage docPage = selPage.switchToDocHeader(Dictionary.DEFAULT_WAIT_TIME);
        //checks
        Assert.assertFalse(docPage.getIsInFailedState(), "Could not open Documentation Page for expected time");

        logger.info("Switch to Documentation page test finished");
        releaseContainer(wdcontainer);
    }

    @Test(invocationCount=5)
    public void testOpenDoc() {
        logger.info("Starting opening Documentation page test");
        //init
        WebDriverContainer wdcontainer = initDriver();
        DocumentationPage docPage = new DocumentationPage(wdcontainer.getDriver(), getURL() + "/documentation");
        //logic
        docPage.open().waitPage(Dictionary.DEFAULT_WAIT_TIME);
        //checks
        Assert.assertFalse(docPage.getIsInFailedState(), "Could not open Documentation Page for expected time");

        logger.info("Opening Documentation page test finished");
        releaseContainer(wdcontainer);
    }

    //@Test(dependsOnMethods = {"testSwitchToDoc"})
    @Test(invocationCount=4)
    @Parameters({"editDocLink"})
    public void editDocumentationPage(String expectedUrl) {
        logger.info("Starting editing documentation page");
        //init
        WebDriverContainer wdcontainer = initDriver();
        DocumentationPage docPage = new DocumentationPage(wdcontainer.getDriver(), getURL() + "/documentation");
        //logic
        docPage.open();
        GitHubAuthPage gitHubPage = docPage.editingDocumentationPage(Dictionary.DEFAULT_WAIT_TIME);
        //checks
        Assert.assertNotNull(gitHubPage, "Could not open Edit Documentation Page for expected time");
        Assert.assertEquals(gitHubPage.getURL(), expectedUrl);

        logger.info("Editing documentation page test finished");
        releaseContainer(wdcontainer);
    }

}
