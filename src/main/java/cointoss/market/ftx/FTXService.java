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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.market.Exchange;
import cointoss.market.TimestampBasedMarketServiceSupporter;
import cointoss.order.OrderBookPage;
import cointoss.order.OrderBookPageChanges;
import cointoss.ticker.data.Liquidation;
import cointoss.ticker.data.OpenInterest;
import cointoss.util.APILimiter;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import cointoss.util.Primitives;
import cointoss.util.arithmetic.Num;
import kiss.I;
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
        long[] previous = new long[3];

        return new Signal<JSON>((observer, disposer) -> {
            long slidingTime = 2 * 60 * 60;
            long startTime = Support.computeEpochTime(startId) + 1;
            long endTime = startTime + slidingTime;
            LinkedList<List<JSON>> jsons = new LinkedList();

            // Retrieve the execution history between the specified dates and times in small chunks.
            while (!disposer.isDisposed()) {
                List<JSON> executions = new ArrayList(1024);
                jsons.add(executions);

                call("GET", "markets/" + marketName + "/trades?start_time=" + startTime + "&end_time=" + endTime)
                        .flatIterable(e -> e.find("result", "*"))
                        .waitForTerminate()
                        .to(executions::add, observer::error);

                int size = executions.size();
                if (size == 0) {
                    // slide to the next duration
                    startTime += slidingTime;
                    endTime += slidingTime;

                    if (Chrono.utcNow().toEpochSecond() <= startTime) {
                        // no data
                        break;
                    }
                } else if (size < 5000) {
                    // complete log in this duration
                    break;
                } else {
                    // overflowed, slide to the previous duration
                    endTime = parseTime(executions.get(size - 1).text("time")).toEpochSecond();
                }
            }

            // Since the first one is the most recent value, it is sent in chronological order,
            // starting with the last one.
            Iterator<List<JSON>> iterator = jsons.descendingIterator();
            while (iterator.hasNext()) {
                List<JSON> list = iterator.next();
                for (int i = list.size() - 1; 0 <= i; i--) {
                    observer.accept(list.get(i));
                }
            }
            observer.complete();

            return disposer;
        }).map(json -> createExecution(json, previous));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        long[] previous = new long[3];

        return clientRealtimely().subscribe(new Topic("trades", marketName))
                .flatIterable(json -> json.find("data", "*"))
                .map(json -> createExecution(json, previous));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "markets/" + marketName + "/trades?limit=1").flatIterable(e -> e.find("result", "*"))
                .map(json -> createExecution(json, new long[3]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsBefore(long id) {
        int limit = setting.acquirableExecutionSize;
        long time = Support.computeEpochTime(id);

        return call("GET", "markets/" + marketName + "/trades?limit=" + limit + "&end_time=" + time)
                .flatIterable(e -> e.find("result", "$"))
                .map(json -> createExecution(json, new long[3]));
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
    private Execution createExecution(JSON e, long[] context) {
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
    protected Signal<OrderBookPageChanges> connectOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("orderbook", marketName)).map(json -> createOrderBook(json.get("data")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Liquidation> connectLiquidation() {
        if (setting.type.isSpot()) {
            return I.signal();
        }

        return this.connectExecutionRealtimely()
                .take(e -> e.delay == Execution.DelayHuge)
                .map(e -> Liquidation.with.date(e.date).direction(e.direction.inverse()).size(e.size.doubleValue()).price(e.price));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OpenInterest> provideOpenInterest(ZonedDateTime startExcluded) {
        if (setting.type.isSpot()) {
            return I.signal();
        }

        return call("GET", "futures/" + marketName + "/stats").map(root -> {
            JSON e = root.get("result");
            return OpenInterest.with.date(Chrono.utcNow()).size(e.get(double.class, "openInterest"));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OpenInterest> connectOpenInterest() {
        if (setting.type.isSpot()) {
            return I.signal();
        }

        return Chrono.seconds()
                .takeAt(i -> i % 10 == 0)
                .concatMap(time -> call("GET", "openInterest?symbol=" + marketName))
                .map(e -> OpenInterest.with.date(Chrono.utcByMills(e.get(long.class, "time"))).size(e.get(double.class, "openInterest")));
    }

    public static void main(String[] args) throws InterruptedException {
        double[] volume = new double[3];
        double[] previousOISize = {0};

        FTX.BTC_PERP.executionsRealtimely().to(e -> {
            if (e.isBuy()) {
                volume[0] += e.size.doubleValue();
                volume[2] += e.size.doubleValue() * e.price.doubleValue();
            } else {
                volume[1] += e.size.doubleValue();
                volume[2] += e.size.doubleValue() * e.price.doubleValue();
            }
        });
        I.schedule(1, 1, TimeUnit.SECONDS, true)
                .take(360 * 5)
                .waitForTerminate()
                .concatMap(x -> FTX.BTC_PERP.provideOpenInterest(Chrono.utcNow().minusMinutes(10)))
                .diff((p, n) -> p.size == n.size)
                .to(e -> {
                    double deltaOI = e.size - previousOISize[0];
                    double total = volume[0] + volume[1];
                    double entry = total + deltaOI / 2d;
                    double exit = total - deltaOI / 2d;

                    System.out.println(Chrono.utcNow() + "    " + e + "  B:" + Primitives.roundString(volume[0], 6) + "   S:" + Primitives
                            .roundString(volume[1], 6) + "   Total:" + Primitives
                                    .roundString(volume[0] + volume[1], 6) + "   AvePrice:" + Primitives
                                            .roundString(volume[2] / total, 2) + "  Entry:" + Primitives
                                                    .roundString(entry, 2) + "    Exit:" + Primitives.roundString(exit, 2));
                    volume[0] = 0;
                    volume[1] = 0;
                    volume[2] = 0;
                    previousOISize[0] = e.size;
                });

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

    // /**
    // * {@inheritDoc}
    // */
    // @Override
    // public Signal<OpenInterest> provideOpenInterest(ZonedDateTime startExcluded) {
    // return call("GET", "options/historical_open_interest/" + "BTC" +
    // "?limit=200&start_time=1559881511").map(e -> {
    // System.out.println(e);
    // return null;
    // });
    // }

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