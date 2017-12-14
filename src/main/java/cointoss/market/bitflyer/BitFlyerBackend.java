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

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.HmacUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import cointoss.Execution;
import cointoss.Market;
import cointoss.MarketBackend;
import cointoss.Order;
import cointoss.Order.State;
import cointoss.Position;
import cointoss.order.OrderBookChange;
import cointoss.order.OrderUnit;
import cointoss.util.Num;
import filer.Filer;
import kiss.Disposable;
import kiss.I;
import kiss.JSON;
import kiss.Signal;

/**
 * @version 2017/12/02 11:49:35
 */
class BitFlyerBackend implements MarketBackend {

    /** The server checking thread. */
    private static final ScheduledExecutorService checker = Executors.newSingleThreadScheduledExecutor(run -> {
        Thread thread = new Thread(run);
        thread.setName("Server Checker");
        thread.setDaemon(true);
        return thread;
    });

    /** The api url. */
    static final String api = "https://api.bitflyer.jp";

    /** UTC */
    static final ZoneId zone = ZoneId.of("UTC");

    /** The market type. */
    private BitFlyer type;

    /** The key. */
    private final String accessKey;

    /** The token. */
    private final String accessToken;

    private Disposable disposer = Disposable.empty();

    /** The shared source. */
    private Signal<OrderBookChange> board = realtimeOrderBook();

    /**
     * @param type
     */
    BitFlyerBackend(BitFlyer type) {
        List<String> lines = Filer.read(".log/bitflyer/key.txt").toList();

        this.type = type;
        this.accessKey = lines.get(0);
        this.accessToken = lines.get(1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(Market market, Signal<Execution> log) {
        disposer.add(log.to(market::tick));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
        disposer.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<String> request(Order order) {
        // {
        // "product_code": "BTC_JPY",
        // "child_order_type": "LIMIT",
        // "side": "BUY",
        // "price": 30000,
        // "size": 0.1,
        // "minute_to_expire": 10000,
        // "time_in_force": "GTC"
        // }
        ChildOrderRequest request = new ChildOrderRequest();
        request.child_order_type = order.isLimit() ? "LIMIT" : "MARKET";
        request.minute_to_expire = 60 * 24;
        request.price = order.price.toInt();
        request.product_code = type.name();
        request.side = order.side().name();
        request.size = order.size.toDouble();
        switch (order.quantity()) {
        case GoodTillCanceled:
            request.time_in_force = "GTC";
            break;

        case ImmediateOrCancel:
            request.time_in_force = "IOC";
            break;

        case FillOrKill:
            request.time_in_force = "FOK";
            break;
        }

        return call("POST", "/v1/me/sendchildorder", request, "child_order_acceptance_id", String.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<String> cancel(String childOrderId) {
        ChildCancelRequest request = new ChildCancelRequest();
        request.child_order_acceptance_id = childOrderId;
        request.product_code = type.name();

        return call("POST", "/v1/me/cancelchildorder", request, null, null).mapTo(childOrderId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> getOrderBy(String id) {
        return call("GET", "/v1/me/getchildorders?product_code=" + type.name() + "&child_order_acceptance_id=" + id, "", "*", Order.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> getOrders() {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> getOrdersBy(State state) {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Position> getPositions() {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> getExecutions() {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> getBaseCurrency() {
        return call("GET", "/v1/me/getbalance", "", "*", CurrencyState.class).take(unit -> unit.currency_code.equals("JPY"))
                .map(c -> c.available);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> getTargetCurrency() {
        return call("GET", "/v1/me/getbalance", "", "*", CurrencyState.class).take(unit -> unit.currency_code.equals("BTC"))
                .map(c -> c.available);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookChange> getOrderBook() {
        return snapshotOrderBook().merge(board);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Health> getHealth() {
        return health;
    }

    private final Signal<Health> health = new Signal<Health>((observer, disposer) -> {
        checker.scheduleAtFixedRate(() -> {
            call("GET", "/v1/gethealth?product_code=" + type, "", "", ServerHealth.class).map(health -> Health.valueOf(health.status))
                    .to(observer::accept);
        }, 0, 30, TimeUnit.SECONDS);
        return disposer.add(checker::shutdown);
    }).share();

    /**
     * Snapshot order book info.
     * 
     * @return
     */
    private Signal<OrderBookChange> snapshotOrderBook() {
        return call("GET", "/v1/board?product_code=" + type, "", "", OrderBookChange.class);
    }

    /**
     * Realtime order book info.
     * 
     * @return
     */
    private Signal<OrderBookChange> realtimeOrderBook() {
        return new Signal<OrderBookChange>((observer, disposer) -> {
            PNConfiguration config = new PNConfiguration();
            config.setSubscribeKey("sub-c-52a9ab50-291b-11e5-baaa-0619f8945a4f");

            PubNub pubNub = new PubNub(config);
            pubNub.addListener(new SubscribeCallback() {

                /**
                 * @param pubnub
                 * @param status
                 */
                @Override
                public void status(PubNub pubnub, PNStatus status) {
                }

                /**
                 * @param pubnub
                 * @param presence
                 */
                @Override
                public void presence(PubNub pubnub, PNPresenceEventResult presence) {
                }

                /**
                 * @param pubnub
                 * @param message
                 */
                @Override
                public void message(PubNub pubnub, PNMessageResult message) {
                    if (message.getChannel() != null) {
                        JsonNode node = message.getMessage();

                        OrderBookChange board = new OrderBookChange();
                        board.mid_price = Num.of(node.get("mid_price").asLong());

                        JsonNode asks = node.get("asks");

                        for (int i = 0; i < asks.size(); i++) {
                            JsonNode ask = asks.get(i);
                            board.asks.add(new OrderUnit(Num.of(ask.get("price").asDouble()), Num.of(ask.get("size").asDouble())));
                        }

                        JsonNode bids = node.get("bids");

                        for (int i = 0; i < bids.size(); i++) {
                            JsonNode bid = bids.get(i);
                            board.bids.add(new OrderUnit(Num.of(bid.get("price").asDouble()), Num.of(bid.get("size").asDouble())));
                        }
                        observer.accept(board);
                    }
                }
            });
            pubNub.subscribe().channels(I.list("lightning_board_" + type)).execute();

            return disposer.add(() -> {
                pubNub.unsubscribeAll();
                pubNub.destroy();
            });
        });
    }

    /**
     * Call private API.
     */
    private <M> Signal<M> call(String method, String path, Object body, String selector, Class<M> type) {
        StringBuilder builder = new StringBuilder();
        I.write(body, builder);

        return call(method, path, builder.toString(), selector, type);
    }

    /**
     * Call private API.
     */
    private <M> Signal<M> call(String method, String path, String body, String selector, Class<M> type) {
        return new Signal<>((observer, disposer) -> {
            String timestamp = String.valueOf(ZonedDateTime.now(zone).toEpochSecond());
            String sign = HmacUtils.hmacSha256Hex(accessToken, timestamp + method + path + body);

            HttpUriRequest request = null;

            if (method.equals("GET")) {
                request = new HttpGet(api + path);
                request.addHeader("ACCESS-KEY", accessKey);
                request.addHeader("ACCESS-TIMESTAMP", timestamp);
                request.addHeader("ACCESS-SIGN", sign);
            } else if (method.equals("POST")) {
                request = new HttpPost(api + path);
                request.addHeader("ACCESS-KEY", accessKey);
                request.addHeader("ACCESS-TIMESTAMP", timestamp);
                request.addHeader("ACCESS-SIGN", sign);
                request.addHeader("Content-Type", "application/json");
                ((HttpPost) request).setEntity(new StringEntity(body, StandardCharsets.UTF_8));
            } else {
                // If this exception will be thrown, it is bug of this program. So we must rethrow
                // the wrapped error in here.
                throw new Error();
            }

            try (CloseableHttpClient client = HttpClientBuilder.create().disableCookieManagement().build(); //
                    CloseableHttpResponse response = client.execute(request)) {

                int status = response.getStatusLine().getStatusCode();
                String value = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (status == HttpStatus.SC_OK) {
                    if (type == null) {
                        observer.accept(null);
                    } else {
                        JSON json = I.json(value);

                        if (selector == null || selector.isEmpty()) {
                            observer.accept(json.to(type));
                        } else {
                            json.find(selector, type).to(observer::accept);
                        }
                    }
                } else {
                    observer.error(new Error("HTTP Status " + status + " " + value));
                }
            } catch (Throwable e) {
                observer.error(e);
            }
            observer.complete();

            return disposer;
        });
    }

    /**
     * @version 2017/11/13 13:09:00
     */
    @SuppressWarnings("unused")
    private static class ChildOrderRequest {

        public String product_code;

        public String child_order_type;

        public String side;

        public int price;

        public double size;

        public int minute_to_expire;

        public String time_in_force;
    }

    /**
     * @version 2017/11/13 13:09:00
     */
    @SuppressWarnings("unused")
    private static class ChildCancelRequest {

        public String product_code;

        public String child_order_acceptance_id;
    }

    /**
     * @version 2017/11/28 9:28:38
     */
    private static class CurrencyState {

        /** The currency code. */
        public String currency_code;

        /** The total currency amount. */
        public Num amount;

        /** The available currency amount. */
        public Num available;
    }

    /**
     * @version 2017/12/14 16:24:28
     */
    private static class ServerHealth {

        /** The server status. */
        public String status;
    }
}
