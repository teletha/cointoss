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

import java.util.ArrayList;
import java.util.Objects;

import antibug.WebSocketServer;
import antibug.WebSocketServer.WebSocketClient;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.order.OrderState;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.MockNetwork;
import cointoss.util.Num;
import kiss.JSON;

class MockBitFlyerService extends BitFlyerService {

    /** The mockable network. */
    private final MockNetwork mockNetwork;

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

        network = mockNetwork = new MockNetwork();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EfficientWebSocket realtimely() {
        return super.realtimely().clone().enableDebug(websocketServer.httpClient());
    }

    /**
     * Set next response.
     * 
     * @param orderId
     */
    protected void requestWillResponse(String... orderIds) {
        mockNetwork.request("/v1/me/sendchildorder").willResponse((Object[]) orderIds);
    }

    /**
     * Set next response.
     * 
     * @param order
     * @param acceptanceId
     */
    protected void ordersWillResponse(Order order, String acceptanceId) {
        ChildOrderResponse o = new ChildOrderResponse();
        o.child_order_acceptance_id = acceptanceId;
        o.child_order_id = acceptanceId;
        o.side = order.direction;
        o.price = order.price;
        o.average_price = order.price;
        o.size = order.size;
        o.child_order_state = OrderState.ACTIVE;
        o.child_order_date = Chrono.DateTimeWithT.format(Chrono.utcNow());
        o.outstanding_size = order.size;
        o.executed_size = Num.ZERO;

        ChildOrderResponseList array = new ChildOrderResponseList();
        array.add(o);

        mockNetwork.request("/v1/me/getchildorders").willResponse(array);
    }

    /**
     * Root element.
     */
    @SuppressWarnings("serial")
    private static class ChildOrderResponseList extends ArrayList<ChildOrderResponse> {
    }

    /**
     * Set next response.
     * 
     * @param exe
     * @param buyerId
     * @param sellerId
     */
    protected void executionWillResponse(Execution exe, String buyerId, String sellerId) {
        Objects.requireNonNull(exe);
        Objects.requireNonNull(buyerId);
        Objects.requireNonNull(sellerId);

        JSON o = new JSON();
        o.set("id", exe.id);
        o.set("side", exe.direction.name());
        o.set("price", exe.price.doubleValue());
        o.set("size", exe.size.doubleValue());
        o.set("exec_date", BitFlyerService.RealTimeFormat.format(exe.date) + "Z");
        o.set("buy_child_order_acceptance_id", buyerId);
        o.set("sell_child_order_acceptance_id", sellerId);

        JSON root = new JSON();
        root.set("0", o);

        mockNetwork.connect("wss://ws.lightstream.bitflyer.com/json-rpc").willResponse(root);
    }
}