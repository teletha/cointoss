/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.ftx;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.market.Exchange;
import cointoss.market.TimestampBasedMarketServiceSupporter;
import cointoss.order.OrderBookPage;
import cointoss.order.OrderBookPageChanges;
import cointoss.util.APILimiter;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import cointoss.util.arithmetic.Num;
import kiss.JSON;
import kiss.Signal;

public class FTXService extends MarketService {

    private static final TimestampBasedMarketServiceSupporter Support = new TimestampBasedMarketServiceSupporter(1000, false);

    /** The realtime data format */
    private static final DateTimeFormatter TimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

    /** The bitflyer API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(20).refresh(Duration.ofSeconds(1));

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://ftx.com/ws/")
            .extractId(json -> json.has("type", "update") ? json.text("channel") + "@" + json.text("market") : null)
            .recconnectIf(json -> json.has("code", "20001"))
            .stopRecconnectIf(json -> json.has("code", "400") && json.has("msg", "Already subscribed"))
            .ignoreMessageIf(json -> json.has("type", "partial"));

    /**
     * @param marketName
     * @param setting
     */
    protected FTXService(String marketName, MarketSetting setting) {
        super(Exchange.FTX, marketName, setting);
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
    public Signal<Execution> executions(long startId, long endId) {
        AtomicLong increment = new AtomicLong();
        long[] previous = new long[3];

        long startTime = Support.computeEpochTime(startId) + 1;
        long[] endTime = {Support.computeEpochTime(endId)};

        return new Signal<JSON>((observer, disposer) -> {
            int latestSize = 0;
            List<JSON> executions = new ArrayList(setting.acquirableExecutionSize);

            // Retrieve the execution history between the specified dates and times in small chunks.
            while (!disposer.isDisposed()) {
                call("GET", "markets/" + marketName + "/trades?limit=200&start_time=" + startTime + "&end_time=" + endTime[0])
                        .flatIterable(e -> e.find("result", "*"))
                        .waitForTerminate()
                        .toCollection(executions);

                int size = executions.size();
                if (latestSize == size) {
                    break;
                } else {
                    latestSize = size;
                    endTime[0] = parseTime(executions.get(size - 1).text("time")).toEpochSecond();
                }
            }

            // Since the first one is the most recent value, it is sent in chronological order,
            // starting with the last one.
            for (int i = latestSize - 1; 0 <= i; i--) {
                observer.accept(executions.get(i));
            }
            observer.complete();

            return disposer;
        }).map(json -> createExecution(json, increment, previous));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        AtomicLong increment = new AtomicLong();
        long[] previous = new long[3];

        return clientRealtimely().subscribe(new Topic("trades", marketName))
                .flatIterable(json -> json.find("data", "*"))
                .map(json -> createExecution(json, increment, previous));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "markets/" + marketName + "/trades?limit=1").flatIterable(e -> e.find("result", "*"))
                .map(json -> createExecution(json, new AtomicLong(), new long[3]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsBefore(long id) {
        return call("GET", "markets/" + marketName + "/trades?limit=1&end_time=" + Support.computeEpochTime(id))
                .flatIterable(e -> e.find("result", "*"))
                .map(json -> createExecution(json, new AtomicLong(), new long[3]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkEquality(Execution one, Execution other) {
        return one.info.equals(other.info);
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param json
     * @param context
     * @return
     */
    private Execution createExecution(JSON e, AtomicLong increment, long[] context) {
        Direction side = e.get(Direction.class, "side");
        Num size = e.get(Num.class, "size");
        Num price = e.get(Num.class, "price");
        ZonedDateTime date = parseTime(e.text("time"));

        return Support.createExecution(side, size, price, date, context)
                .assignDelay(e.get(Boolean.class, "liquidation") ? Execution.DelayHuge : Execution.DelayInestimable)
                .assignInfo(e.text("id"));
    }

    /**
     * Parse time format.
     * 
     * @param time
     * @return
     */
    private ZonedDateTime parseTime(String time) {
        switch (time.length()) {
        case 32: // 2019-10-04T06:06:21.353533+00:00
            time = time.substring(0, 26);
            break;

        case 25: // 2019-10-04T06:07:30+00:00
            time = time.substring(0, 19).concat(".000000");
            break;

        default:
            throw new IllegalArgumentException("Unexpected value: " + time);
        }
        return LocalDateTime.parse(time, TimeFormat).atZone(Chrono.UTC);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookPageChanges> orderBook() {
        return call("GET", "markets/" + marketName + "/orderbook?depth=100").map(json -> json.get("result")).map(this::createOrderBook);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> createOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("orderbook", marketName)).map(json -> createOrderBook(json.get("data")));
    }

    /**
     * Convert JSON to {@link OrderBookPageChanges}.
     * 
     * @param array
     * @return
     */
    private OrderBookPageChanges createOrderBook(JSON pages) {
        OrderBookPageChanges change = new OrderBookPageChanges();

        for (JSON bid : pages.find("bids", "*")) {
            Num price = bid.get(Num.class, "0");
            double size = Double.parseDouble(bid.text("1"));

            change.bids.add(new OrderBookPage(price, size));
        }

        for (JSON ask : pages.find("asks", "*")) {
            Num price = ask.get(Num.class, "0");
            double size = Double.parseDouble(ask.text("1"));

            change.asks.add(new OrderBookPage(price, size));
        }
        return change;
    }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JSON> call(String method, String path) {
        Builder builder = HttpRequest.newBuilder(URI.create("https://ftx.com/api/" + path));

        return Network.rest(builder, Limit, client()).retryWhen(retryPolicy(10, "FTX RESTCall"));
    }

    /**
     * 
     */
    static class Topic extends IdentifiableTopic<Topic> {

        public String op = "subscribe";

        public String channel;

        public String market;

        /**
         * @param channel
         * @param market
         */
        private Topic(String channel, String market) {
            super(channel + "@" + market, topic -> topic.op = "unsubscribe");

            this.channel = channel;
            this.market = market;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean verifySubscribedReply(JSON reply) {
            return "subscribed".equals(reply.text("type")) && channel.equals(reply.text("channel")) && market.equals(reply.text("market"));
        }
    }
}