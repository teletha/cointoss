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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.gson.JsonElement;

import signalj.LogLevel;
import signalj.Logger;
import signalj.hubs.HubConnection;
import signalj.hubs.HubProxy;

/**
 * @version 2018/01/28 23:46:13
 */
public class Websocket {

    public static void main(String[] args) throws Exception {

        // Create a new console logger
        Logger logger = new Logger() {

            @Override
            public void log(String message, LogLevel level) {
                // System.out.println(message);
            }
        };

        Path file = Paths.get(".log/bitflyer/key.txt");
        List<String> lines = Files.readAllLines(file);
        String id = lines.get(4);
        String token = lines.get(5);

        String uri = "https://lightning.bitflyer.jp";

        // Connect to the server
        HubConnection conn = new HubConnection(uri, "account_id=" + id + "&token=" + token + "&products=FX_BTC_JPY,heartbeat", true, logger);

        // Subscribe to the error event
        conn.error(e -> {
            e.printStackTrace();
        });

        // Subscribe to the closed event
        conn.stateChanged((o, n) -> {
            System.out.println("Change from " + o + " to " + n + ".");
        });

        conn.received(m -> {
            System.out.println(m);
        });

        // Create the hub proxy
        HubProxy proxy = conn.createHubProxy("BFEXHub");

        proxy.subscribe("ReceiveOrderUpdates").addReceivedHandler(jsons -> {
            for (JsonElement json : jsons) {
                System.out.println(json);
            }
        });

        // Start the connection
        conn.start();
    }
}
