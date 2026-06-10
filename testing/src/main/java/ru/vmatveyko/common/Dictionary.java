package ru.vmatveyko.common;

import org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode;

public class Dictionary {

    //should cookies and local storage be cleared
    public static final Boolean CLEAR_COOKIES_AND_STORAGE = false;

    //hold browser after test
    public static final Boolean HOLD_BROWSER_OPEN = false;

    //default wait webelement time
    public static final long DEFAULT_WAIT_TIME = 1000L;

    //states to wait element
    public enum States {
        Clickable, Visible, Enable
    }

    //container recording mode for web tests
    public static final VncRecordingMode CONTAINER_RECORDING_MODE = VncRecordingMode.SKIP;

}
