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
 * @version 2018/02/10 14:53:25
 */
public interface HubConnection {
    void connect();

    void disconnect();

    void addListener(HubConnectionListener listener);

    void removeListener(HubConnectionListener listener);

    void subscribeToEvent(String eventName, HubEventListener eventListener);

    void unSubscribeFromEvent(String eventName, HubEventListener eventListener);

    void invoke(String event, Object... parameters);
}
