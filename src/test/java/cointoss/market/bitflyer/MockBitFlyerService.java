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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cointoss.Execution;
import kiss.Signal;
import kiss.Signaling;

/**
 * @version 2018/04/29 21:19:34
 */
class MockBitFlyerService extends BitFlyerService {

    private final Map<String, Object> responses = new HashMap();

    private final Signaling<JsonElement> websocket = new Signaling();

    /**
     * 
     */
    MockBitFlyerService() {
        super(BitFlyer.FX_BTC_JPY, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected <M> Signal<M> call(String method, String path, String body, String selector, Class<M> type) {
        Object response = responses.remove(path);

        if (response == null) {
            throw new AssertionError("[" + path + "] requires valid response.");
        } else {
            return new Signal<M>((observer, disposer) -> {
                observer.accept((M) response);
                return disposer;
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<JsonElement> websocket(String uri, String channel) {
        return websocket.expose;
    }

    /**
     * Set next response.
     * 
     * @param orderId
     */
    protected void nextRequest(String orderId) {
        Objects.requireNonNull(orderId);
        responses.put("/v1/me/sendchildorder", orderId);
    }

    /**
     * Set next response.
     * 
     * @param exe
     * @param buyerId
     * @param sellerId
     */
    protected void nextExecution(Execution exe, String buyerId, String sellerId) {
        Objects.requireNonNull(exe);
        Objects.requireNonNull(buyerId);
        Objects.requireNonNull(sellerId);

        JsonObject o = new JsonObject();
        o.addProperty("id", exe.id);
        o.addProperty("side", exe.side.name());
        o.addProperty("price", exe.price.toDouble());
        o.addProperty("size", exe.size.toDouble());
        o.addProperty("exec_date", exe.exec_date.toString());
        o.addProperty("buy_child_order_acceptance_id", buyerId);
        o.addProperty("sell_child_order_acceptance_id", sellerId);

        JsonArray root = new JsonArray();
        root.add(o);

        websocket.accept(root);
    }
}
