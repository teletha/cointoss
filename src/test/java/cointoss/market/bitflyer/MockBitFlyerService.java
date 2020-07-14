/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitflyer;

import java.net.http.HttpClient;

import com.pgssoft.httpclient.HttpClientMock;
import com.pgssoft.httpclient.RecordableHttpClientMock;

import antibug.WebSocketServer;
import antibug.WebSocketServer.WebSocketClient;
import cointoss.MarketSetting;
import cointoss.util.EfficientWebSocket;
import cointoss.util.Num;

class MockBitFlyerService extends BitFlyerService {

    /** The mocked http client interface. */
    protected final HttpClientMock httpClient = RecordableHttpClientMock.build();

    /** The mocked websocket server. */
    protected final WebSocketServer websocketServer = new WebSocketServer();

    /** The websocket client interface. */
    protected final WebSocketClient websocketClient = websocketServer.websocketClient();

    /**
     * 
     */
    MockBitFlyerService() {
        super("FX_BTC_JPY", true, MarketSetting.with.baseCurrencyMinimumBidPrice(Num.ONE)
                .targetCurrencyMinimumBidSize(Num.of("0.01"))
                .orderBookGroupRanges(Num.ONE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HttpClient client() {
        return httpClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EfficientWebSocket clientRealtimely() {
        return super.clientRealtimely().clone().enableDebug(websocketServer.httpClient());
    }
}