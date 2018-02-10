/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer.signalr;

/**
 * @version 2018/02/10 14:53:45
 */
public interface HubConnectionListener {
    void onConnected();

    void onDisconnected();

    void onMessage(HubMessage message);

    void onError(Exception exception);
}