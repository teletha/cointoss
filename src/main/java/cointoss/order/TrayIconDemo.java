/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.order;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.net.MalformedURLException;

/**
 * @version 2018/04/16 21:31:51
 */
public class TrayIconDemo {
    public static void main(String[] args) throws AWTException, MalformedURLException {
        if (SystemTray.isSupported()) {
            TrayIconDemo td = new TrayIconDemo();
            td.displayTray();
        } else {
            System.err.println("System tray not supported!");
        }
    }

    public void displayTray() throws AWTException, MalformedURLException {
        // Obtain only one instance of the SystemTray object
        SystemTray tray = SystemTray.getSystemTray();

        // If the icon is a file
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        // Alternative (if the icon is on the classpath):
        // Image image = Toolkit.getToolkit().createImage(getClass().getResource("icon.png"));

        TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
        // Let the system resize the image if needed
        trayIcon.setImageAutoSize(true);
        // Set tooltip text for the tray icon
        trayIcon.setToolTip("System tray icon demo");
        tray.add(trayIcon);

        trayIcon.displayMessage("Hello, World", "notification demo", MessageType.INFO);
        trayIcon.displayMessage("Hello, World2", "notification demo2", MessageType.ERROR);
    }
}
