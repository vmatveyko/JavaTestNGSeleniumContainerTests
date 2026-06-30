package ru.vmatveyko.common;

import org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode;

public class Dictionary {

    //should cookies and local storage be cleared
    public static final boolean CLEAR_COOKIES_AND_STORAGE = false;

    //hold browser and container after test if test failed
    public static final boolean HOLD_BROWSER_OPEN = false;

    //default wait webelement time
    public static final long DEFAULT_WAIT_TIME = 1000L;

    //wait time for free container (seconds)
    public static final long WAIT_TIME_FOR_CONTAINER = 200L;

    //states to wait element
    public enum States {
        Clickable, Visible, Enable
    }

    //container recording mode for web tests
    public static final VncRecordingMode CONTAINER_RECORDING_MODE = VncRecordingMode.SKIP;

}
