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

    /**
     * Start recording mode.
     */
    public void record() {
        httpClient.debugOn();
    }

    // /**
    // * Set next response.
    // *
    // * @param orderId
    // */
    // protected void requestWillResponse(String... orderIds) {
    // httpServer.request("/v1/me/sendchildorder").willResponse((Object[]) orderIds);
    // }
    //
    // /**
    // * Set next response.
    // *
    // * @param order
    // * @param acceptanceId
    // */
    // protected void ordersWillResponse(Order order, String acceptanceId) {
    // ChildOrderResponse o = new ChildOrderResponse();
    // o.child_order_acceptance_id = acceptanceId;
    // o.child_order_id = acceptanceId;
    // o.side = order.direction;
    // o.price = order.price;
    // o.average_price = order.price;
    // o.size = order.size;
    // o.child_order_state = OrderState.ACTIVE;
    // o.child_order_date = Chrono.DateTimeWithT.format(Chrono.utcNow());
    // o.outstanding_size = order.size;
    // o.executed_size = Num.ZERO;
    //
    // ChildOrderResponseList array = new ChildOrderResponseList();
    // array.add(o);
    //
    // httpServer.request("/v1/me/getchildorders").willResponse(array);
    // }

    // /**
    // * Set next response.
    // *
    // * @param exe
    // * @param buyerId
    // * @param sellerId
    // */
    // protected void executionWillResponse(Execution exe, String buyerId, String sellerId) {
    // Objects.requireNonNull(exe);
    // Objects.requireNonNull(buyerId);
    // Objects.requireNonNull(sellerId);
    //
    // JSON o = new JSON();
    // o.set("id", exe.id);
    // o.set("side", exe.direction.name());
    // o.set("price", exe.price.doubleValue());
    // o.set("size", exe.size.doubleValue());
    // o.set("exec_date", BitFlyerService.RealtimeFormat.format(exe.date) + "Z");
    // o.set("buy_child_order_acceptance_id", buyerId);
    // o.set("sell_child_order_acceptance_id", sellerId);
    //
    // JSON root = new JSON();
    // root.set("0", o);
    //
    // httpServer.connect("wss://ws.lightstream.bitflyer.com/json-rpc").willResponse(root);
    // }
}