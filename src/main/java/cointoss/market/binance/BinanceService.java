/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.binance;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.order.OrderBookPage;
import cointoss.order.OrderBookPageChanges;
import cointoss.order.OrderState;
import cointoss.util.APILimiter;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocket.IdentifiableTopic;
import cointoss.util.Num;
import kiss.I;
import kiss.JSON;
import kiss.Signal;
import okhttp3.Request;

class BinanceService extends MarketService {

    /** The bitflyer API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(600).refresh(Duration.ofMinutes(1));

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = new EfficientWebSocket("wss://stream.binance.com:9443/stream", 25, json -> json
            .text("stream"));

    /** The realtime communicator. */
    private static final EfficientWebSocket RealtimeFuture = new EfficientWebSocket("wss://fstream.binance.com/stream", 25, json -> json
            .text("stream"));

    /** The market type. */
    private final boolean isFutures;

    /**
     * @param marketName
     * @param setting
     */
    BinanceService(String marketName, boolean isFutures, MarketSetting setting) {
        super(isFutures ? "BinanceF" : "Binance", marketName, setting);

        this.isFutures = isFutures;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Integer> delay() {
        return I.signal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<String> request(Order order) {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> cancel(Order order) {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long start, long end) {
        return call("GET", "aggTrades?symbol=" + marketName + "&limit=1000&fromId=" + (start + 1)).flatIterable(e -> e.find("*"))
                .map(this::convert);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        return realtime().subscribe(new Command("aggTrade", marketName)).map(json -> convert(json.get("data")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "aggTrades?symbol=" + marketName + "&limit=1").flatIterable(e -> e.find("*")).map(this::convert);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long estimateInitialExecutionId() {
        return 0;
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
        return call("GET", "depth?symbol=" + marketName + "&limit=1000").map(e -> convertOrderBook(e, "bids", "asks"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> connectOrderBookRealtimely() {
        String bidName = isFutures ? "b" : "bids";
        String askName = isFutures ? "a" : "asks";

        return realtime().subscribe(new Command("depth20@100ms", marketName))
                .map(json -> convertOrderBook(json.get("data"), bidName, askName));
    }

    /**
     * Convert JSON to {@link OrderBookPageChanges}.
     * 
     * @param array
     * @return
     */
    private OrderBookPageChanges convertOrderBook(JSON pages, String bidName, String askName) {
        OrderBookPageChanges change = new OrderBookPageChanges();

        for (JSON bid : pages.find(bidName, "*")) {
            Num price = bid.get(Num.class, "0");
            double size = Double.parseDouble(bid.text("1"));

            change.bids.add(new OrderBookPage(price, size));
        }

        for (JSON ask : pages.find(askName, "*")) {
            Num price = ask.get(Num.class, "0");
            double size = Double.parseDouble(ask.text("1"));

            change.asks.add(new OrderBookPage(price, size));
        }
        return change;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> baseCurrency() {
        return I.signal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> targetCurrency() {
        return I.signal();
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param json
     * @param previous
     * @return
     */
    private Execution convert(JSON e) {
        Direction direction = e.get(Boolean.class, "m") ? Direction.SELL : Direction.BUY;
        Num size = e.get(Num.class, "q");
        Num price = e.get(Num.class, "p");
        long tradeTime = Long.parseLong(e.text("T"));
        ZonedDateTime date = Chrono.utcByMills(tradeTime);
        long tradeId = Long.parseLong(e.text("a"));

        Execution exe = Execution.with.direction(direction, size)
                .id(tradeId)
                .price(price)
                .date(date)
                .consecutive(Execution.ConsecutiveDifference)
                .delay(Execution.DelayInestimable);

        return exe;
    }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JSON> call(String method, String path) {
        String uri = isFutures ? "https://fapi.binance.com/fapi/v1/" : "https://api.binance.com/api/v3/";
        Request request = new Request.Builder().url(uri + path).build();

        return network.rest(request, Limit).retryWhen(retryPolicy(10, "Binance RESTCall"));
    }

    /**
     * Select realtime communicator.
     * 
     * @return
     */
    private EfficientWebSocket realtime() {
        return isFutures ? RealtimeFuture : Realtime;
    }

    /**
     * 
     */
    public static class Command extends IdentifiableTopic {

        public String method = "SUBSCRIBE";

        public List<String> params = new ArrayList();

        public int id = 0;

        private Command(String channel, String market) {
            super(market.toLowerCase() + "@" + channel, "SUBSCRIBE", "UNSUBSCRIBE");
            this.params.add(market.toLowerCase() + "@" + channel);
        }
    }
}