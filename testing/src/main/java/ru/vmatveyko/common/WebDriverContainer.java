package ru.vmatveyko.common;

import org.openqa.selenium.WebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;

import lombok.AllArgsConstructor;
import lombok.Data;

@SuppressWarnings("deprecation")
@AllArgsConstructor
@Data
public class WebDriverContainer {

    private BrowserWebDriverContainer<?> container;
    private WebDriver driver;
    private boolean isLastTestFailed;
    private String containerImage;


    public WebDriverContainer(BrowserWebDriverContainer<?> cont,
        WebDriver driver, String image) {
        this.container = cont;
        this.driver = driver;
        this.isLastTestFailed = false;
        this.containerImage = image;
    }

}
