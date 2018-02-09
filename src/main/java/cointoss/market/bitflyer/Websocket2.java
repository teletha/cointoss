/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import com.github.signalr4j.client.LogLevel;
import com.github.signalr4j.client.Logger;
import com.github.signalr4j.client.hubs.HubConnection;
import com.github.signalr4j.client.hubs.HubProxy;

/**
 * @version 2018/01/28 23:46:13
 */
public class Websocket2 {

    public static void main(String[] args) {

        // Create a new console logger
        Logger logger = new Logger() {

            @Override
            public void log(String message, LogLevel level) {
                // System.out.println(message);
            }
        };

        String uri = "https://lightning.bitflyer.jp/signalr";

        // Connect to the server
        HubConnection conn = new HubConnection(uri, "", false, logger);

        // Subscribe to the error event
        conn.error(e -> {
            e.printStackTrace();
        });

        // Subscribe to the connected event
        conn.connected(() -> {
            System.out.println("Connect");
        });

        // Subscribe to the closed event
        conn.closed(() -> {
            System.out.println("Close");
        });

        conn.stateChanged((o, n) -> {
            System.out.println(o + "  to  " + n);
        });

        // Subscribe to the received event
        conn.received(json -> {
            System.out.println(json);
        });

        // Create the hub proxy
        HubProxy proxy = conn.createHubProxy("BFEXHub");
        proxy.subscribe("ReceiveTickers").addReceivedHandler(jsons -> {
            System.out.println(jsons);
        });
        proxy.subscribe("addNewMessageToPage").addReceivedHandler(jsons -> {
            System.out.println(jsons);
        });

        // Start the connection
        conn.start();

    }
}
