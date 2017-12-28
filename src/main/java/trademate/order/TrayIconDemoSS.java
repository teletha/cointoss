/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.order;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;

/**
 * @version 2017/12/28 1:46:26
 */
public class TrayIconDemoSS {

    /**
     * @param args
     * @throws AWTException
     * @throws java.net.MalformedURLException
     */
    public static void main(String[] args) throws AWTException, java.net.MalformedURLException {
        // if (SystemTray.isSupported()) {
        // TrayIconDemoSS td = new TrayIconDemoSS();
        // td.displayTray();
        // } else {
        // System.err.println("System tray not supported!");
        // }

    }

    public void displayTray() throws AWTException, java.net.MalformedURLException {
        // Obtain only one instance of the SystemTray object
        SystemTray tray = SystemTray.getSystemTray();

        // If the icon is a file
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        // Alternative (if the icon is on the classpath):
        // Image image = Toolkit.getToolkit().createImage(getClass().getResource("icon.png"));
        TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
        // Let the system resizes the image if needed
        trayIcon.setImageAutoSize(true);
        // Set tooltip text for the tray icon
        trayIcon.setToolTip("System tray icon demo");
        tray.add(trayIcon);
        trayIcon.displayMessage("Hello, World", "notification demo2", MessageType.WARNING);
        trayIcon.displayMessage("Hello, World", "notification demo1", MessageType.WARNING);
        trayIcon.displayMessage("Hello, World", "notification demo3", MessageType.ERROR);
    }
}
