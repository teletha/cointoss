/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitbank;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.execution.ExecutionLogRepository;
import cointoss.market.Exchange;
import cointoss.order.Order;
import cointoss.order.OrderBookPage;
import cointoss.order.OrderBookPageChanges;
import cointoss.order.OrderState;
import cointoss.util.APILimiter;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.JSON;
import kiss.Signal;

public class BitbankService extends MarketService {

    /** The bitflyer API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(20).refresh(Duration.ofSeconds(1));

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with
            .address("wss://stream.bitbank.cc/socket.io/?EIO=3&transport=websocket")
            .extractId(json -> json.text("room_name"))
            .noServerReply()
            .enableSocketIO()
            .enableDebug();

    /**
     * @param marketName
     * @param setting
     */
    protected BitbankService(String marketName, MarketSetting setting) {
        super(Exchange.BitBank, marketName, setting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EfficientWebSocket clientRealtimely() {
        return Realtime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutionLogRepository externalRepository() {
        return new OfficialRepository(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Integer> delay() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<String> request(Order order) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> cancel(Order order) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long startId, long endId) {
        Object[] previous = new Object[2];

        return call("GET", "trading-records?symbol=" + marketName + "&from=" + startId + "&limit=" + (endId - startId))
                .flatIterable(e -> e.find("result", "*"))
                .map(e -> convert(e, previous));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        Object[] previous = new Object[2];

        return clientRealtimely().subscribe(new Topic("transactions", marketName))
                .flatIterable(json -> json.find("message", "data", "transactions", "*"))
                .map(e -> convert(e, previous));
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param e
     * @param previous
     * @return
     */
    private Execution convert(JSON e, Object[] previous) {
        System.out.println(e);
        Direction side = e.get(Direction.class, "side");
        Num price = e.get(Num.class, "price");
        Num size = e.get(Num.class, "amount");
        ZonedDateTime date = Chrono.utcByMills(Long.parseLong(e.text("executed_at")));
        long id = Long.parseLong(e.text("transaction_id"));
        int consecutive;

        if (date.equals(previous[1])) {
            if (side != previous[0]) {
                consecutive = Execution.ConsecutiveDifference;
            } else if (side == Direction.BUY) {
                consecutive = Execution.ConsecutiveSameBuyer;
            } else {
                consecutive = Execution.ConsecutiveSameSeller;
            }
        } else {
            consecutive = Execution.ConsecutiveDifference;
        }

        previous[0] = side;
        previous[1] = date;

        return Execution.with.direction(side, size).id(id).price(price).date(date).consecutive(consecutive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", marketName + "/transactions/2020120700").flatIterable(e -> e.find("data", "transactions", "*"))
                .map(json -> convert(json, new Object[2]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatestAt(long id) {
        return call("GET", marketName + "/transactions").flatIterable(e -> e.find("data", "transactions", "*"))
                .map(json -> convert(json, new Object[2]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long estimateInitialExecutionId() {
        return call("GET", "trading-records?symbol=" + marketName + "&from=1&limit=1").flatIterable(e -> e.find("result", "*"))
                .first()
                .waitForTerminate()
                .map(json -> Long.parseLong(json.text("id")))
                .to().v;
    }

    private Signal<Execution> executionsAt(ZonedDateTime date) {
        Object[] previous = new Object[2];

        return call("GET", marketName + "/transactions/" + Chrono.DateCompact.format(date))
                .flatIterable(e -> e.find("data", "transactions", "*"))
                .map(e -> convert(e, previous));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> orders() {
        return I.signal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> orders(OrderState state) {
        return I.signal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Order> connectOrdersRealtimely() {
        return I.signal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookPageChanges> orderBook() {
        return call("GET", "orderBook/L2?symbol=" + marketName).map(pages -> {
            OrderBookPageChanges change = new OrderBookPageChanges();
            pages.find("result", "*").forEach(e -> convertOrderBook(change, e));
            return change;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> connectOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("orderBook_200.100ms", marketName)).map(pages -> {
            OrderBookPageChanges change = new OrderBookPageChanges();

            String type = pages.text("type");
            if (type.equals("snapshot")) {
                pages.find("data", "*").forEach(e -> convertOrderBook(change, e));
            } else {
                pages.find("data", "delete", "*").forEach(e -> convertOrderBook(change, e));
                pages.find("data", "update", "*").forEach(e -> convertOrderBook(change, e));
                pages.find("data", "insert", "*").forEach(e -> convertOrderBook(change, e));
            }

            return change;
        });
    }

    /**
     * Convert to {@link OrderBookPage}.
     * 
     * @param changes
     * @param e
     */
    private void convertOrderBook(OrderBookPageChanges changes, JSON e) {
        Num price = e.get(Num.class, "price");
        String sizeValue = e.text("size");
        double size = sizeValue == null ? 0 : Double.parseDouble(sizeValue) / price.doubleValue();

        List<OrderBookPage> books = e.text("side").charAt(0) == 'B' ? changes.bids : changes.asks;
        books.add(new OrderBookPage(price, size));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> baseCurrency() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> targetCurrency() {
        return null;
    }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JSON> call(String method, String path) {
        Builder builder = HttpRequest.newBuilder(URI.create("https://public.bitbank.cc/" + path));
        System.out.println(builder.build());
        return Network.rest(builder, Limit, client()).retryWhen(retryPolicy(10, "Bybit RESTCall"));
    }

    /**
     * 
     */
    static class Topic extends IdentifiableTopic<Topic> {

        /**
         * @param channel
         * @param market
         */
        private Topic(String channel, String market) {
            super(channel + "_" + market, topic -> {
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean verifySubscribedReply(JSON reply) {
            return false;
        }
    }

    /**
     * 
     */
    private class OfficialRepository extends ExecutionLogRepository {

        /**
         * @param service
         */
        private OfficialRepository(MarketService service) {
            super(service);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<ZonedDateTime> collect() {
            ZonedDateTime today = Chrono.utcToday();

            return I.signal(Chrono.utc(2018, 1, 1)).recurse(day -> day.plusDays(1)).takeWhile(day -> !day.isEqual(today));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Execution> convert(ZonedDateTime date) {
            return executionsAt(date);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Bitbank.BTC_JPY.executionLatest().to(e -> {
            System.out.println(e);
        });

        Thread.sleep(1000 * 10);
    }
}