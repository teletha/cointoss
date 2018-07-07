/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import java.util.Objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cointoss.Execution;
import cointoss.util.MockNetwork;

/**
 * @version 2018/04/29 21:19:34
 */
class MockBitFlyerService extends BitFlyerService {

    /** The mockable network. */
    private final MockNetwork mockNetwork;

    /**
     * 
     */
    MockBitFlyerService() {
        super("FX_BTC_JPY", true);

        network = mockNetwork = new MockNetwork();
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
     * @param exe
     * @param buyerId
     * @param sellerId
     */
    protected void executionWillResponse(Execution exe, String buyerId, String sellerId) {
        Objects.requireNonNull(exe);
        Objects.requireNonNull(buyerId);
        Objects.requireNonNull(sellerId);

        JsonObject o = new JsonObject();
        o.addProperty("id", exe.id);
        o.addProperty("side", exe.side.name());
        o.addProperty("price", exe.price.toDouble());
        o.addProperty("size", exe.size.toDouble());
        o.addProperty("exec_date", BitFlyerService.RealTimeExecutionFormat.format(exe.exec_date) + "Z");
        o.addProperty("buy_child_order_acceptance_id", buyerId);
        o.addProperty("sell_child_order_acceptance_id", sellerId);

        JsonArray root = new JsonArray();
        root.add(o);

        mockNetwork.connect("wss://ws.lightstream.bitflyer.com/json-rpc").willResponse(root);
    }
}
