package ru.vmatveyko.common;

import org.openqa.selenium.WebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("deprecation")
public class WebDriverContainer {

    @Getter @Setter
    private BrowserWebDriverContainer<?> container;
    @Getter @Setter
    private WebDriver driver;


    public WebDriverContainer(BrowserWebDriverContainer<?> cont, WebDriver driver) {
        this.container = cont;
        this.driver = driver;
    }

}
