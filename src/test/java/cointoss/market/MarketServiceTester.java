/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import com.pgssoft.httpclient.HttpClientMock;
import com.pgssoft.httpclient.RecordableHttpClientMock;

import antibug.Chronus;
import antibug.WebSocketServer;
import antibug.WebSocketServer.WebSocketClient;
import cointoss.MarketService;
import hypatia.Num;
import kiss.I;

@Execution(ExecutionMode.SAME_THREAD)
public abstract class MarketServiceTester<S extends MarketService> {

    static {
        I.load(Num.class);
    }

    /** The mocked http client interface. */
    protected final HttpClientMock httpClient = RecordableHttpClientMock.build();

    /** The mocked websocket server. */
    protected final WebSocketServer websocketServer = new WebSocketServer();

    /** The websocket client interface. */
    protected final WebSocketClient websocketClient = websocketServer.websocketClient();

    /** The manageable scheduler. */
    protected final Chronus chronus = new Chronus();

    /** The debug state. */
    protected boolean usedRealWebSocket;

    /** The testing {@link MarketService} which can mock the network access.. */
    protected S service = constructMarketService();

    @AfterEach
    void after() throws Exception {
        if (usedRealWebSocket) {
            Thread.sleep(12500);
        }
    }

    protected abstract S constructMarketService();

    /**
     * Notify no implementation error.
     */
    void notImplemented() {
        throw new UnsupportedOperationException("Please implement test case");
    }
}