/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.okex;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.execution.ExecutionLogRepository;
import cointoss.market.Exchange;
import cointoss.market.TimestampBasedMarketServiceSupporter;
import cointoss.order.OrderBookPage;
import cointoss.order.OrderBookPageChanges;
import cointoss.util.APILimiter;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.JSON;
import kiss.Signal;
import kiss.Variable;

public class OKExService extends MarketService {

    private static final TimestampBasedMarketServiceSupporter Support = new TimestampBasedMarketServiceSupporter(1000);

    private static final DateTimeFormatter RealTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    /** The API limit. */
    private static final APILimiter LIMITER = APILimiter.with.limit(1).refresh(100, MILLISECONDS);

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://real.okex.com:8443/ws/v3").extractId(json -> {
        List<String> result = json.find(String.class, "data", "0", "instrument_id");
        if (result.isEmpty()) {
            return "";
        } else {
            return json.text("table") + ":" + result.get(0);
        }
    }).enableDebug();

    /**
     * @param marketName
     * @param setting
     */
    protected OKExService(String marketName, MarketSetting setting) {
        super(Exchange.OKEx, marketName, setting);
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
        ZonedDateTime start = Support.computeDateTime(startId);
        Variable<Long> counter = Variable.of(1L);
        long[] prev = new long[3];

        return counter.observing()
                .concatMap(page -> call("GET", "trades?symbol=" + marketName + "&page=" + page))
                .effect(() -> counter.set(v -> v + 1))
                .flatIterable(o -> o.find("data", "list", "*"))
                // The GMO server returns both Taker and Maker histories
                // alternately, so we have to remove the Maker side.
                .skipAt(index -> index % 2 == 0)
                .takeWhile(o -> ZonedDateTime.parse(o.text("timestamp"), RealTimeFormat).isAfter(start))
                .reverse()
                .map(e -> createExecution(e, prev));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        long[] previous = new long[3];

        return clientRealtimely().subscribe(new Topic("spot/trade", marketName))
                .flatIterable(e -> e.find("data", "*"))
                .map(json -> createExecution(json, previous));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "fills?instrument_id=" + marketName + "&limit=1").flatIterable(e -> e.find("*"))
                .map(json -> createExecution(json, new long[3]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatestAt(long id) {
        ZonedDateTime date = Support.computeDateTime(id);
        ExecutionLogRepository repo = externalRepository();

        return repo.convert(date).takeUntil(e -> id <= e.id).waitForTerminate().effect(e -> System.out.println(e));
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param json
     * @param previous
     * @return
     */
    private Execution createExecution(JSON e, long[] previous) {
        System.out.println(e);
        Direction side = e.get(Direction.class, "side");
        Num size = e.get(Num.class, "size");
        Num price = e.get(Num.class, "price");
        ZonedDateTime date = ZonedDateTime.parse(e.text("timestamp"), RealTimeFormat);

        return Support.createExecution(side, size, price, date, previous);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookPageChanges> orderBook() {
        return call("GET", "orderbooks?symbol=" + marketName).map(e -> createOrderBook(e.get("data")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> createOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("orderbooks", marketName)).map(this::createOrderBook);
    }

    /**
     * Convert json to {@link OrderBookPageChanges}.
     * 
     * @param root
     * @return
     */
    private OrderBookPageChanges createOrderBook(JSON root) {
        OrderBookPageChanges changes = new OrderBookPageChanges();
        changes.clearInside = true;

        for (JSON ask : root.find("asks", "*")) {
            Num price = ask.get(Num.class, "price");
            double size = ask.get(double.class, "size");
            changes.asks.add(new OrderBookPage(price, size));
        }
        for (JSON bid : root.find("bids", "*")) {
            Num price = bid.get(Num.class, "price");
            double size = bid.get(double.class, "size");
            changes.bids.add(new OrderBookPage(price, size));
        }

        return changes;
    }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JSON> call(String method, String path) {
        Builder builder = HttpRequest.newBuilder(URI.create("https://www.okex.com/api/spot/v3/" + path));
        return Network.rest(builder, LIMITER, client()).flatMap(json -> {
            if (json.get(int.class, "status") != 0) {
                return I.signalError(new IllegalAccessError(json.get("messages").get("0").text("message_string")));
            } else {
                return I.signal(json);
            }
        }).retryWhen(retryPolicy(10, "GMO RESTCall"));
    }

    /**
     * Subscription topic for websocket communication.
     */
    static class Topic extends IdentifiableTopic<Topic> {

        public String op = "subscribe";

        public List<String> args = new ArrayList();

        private Topic(String channel, String market) {
            super(channel + ":" + market, topic -> topic.op = "unsubscribe");
            this.args.add(id);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean verifySubscribedReply(JSON reply) {
            return reply.text("event").equals("subscribe") && reply.text("channel").equals(id);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        OKEx.BTCUSDT.executionLatest().to(e -> {
            System.out.println(e);
        });

        OKEx.BTCUSDT.executionsRealtimely().to(e -> {
            System.out.println(e);
        });

        Thread.sleep(1000 * 55);
    }
}