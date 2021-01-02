/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import kiss.I;

public class EfficientWebSocketClientJava11 {

    public static void main(String[] args) throws InterruptedException {
        I.http(EfficientWebSocketClient.URL.toString(), ws -> ws.sendText(EfficientWebSocketClient.command, true)).to(e -> {
            System.out.println(e);
        });

        Thread.sleep(1000 * 60 * 30);
    }
}
