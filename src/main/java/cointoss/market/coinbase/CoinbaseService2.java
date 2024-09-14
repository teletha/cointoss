/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.coinbase;

import static java.util.concurrent.TimeUnit.*;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.market.Exchange;
import cointoss.market.TimestampBasedMarketServiceSupporter;
import cointoss.orderbook.OrderBookChanges;
import cointoss.util.APILimiter;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import hypatia.Num;
import kiss.I;
import kiss.JSON;
import kiss.Signal;

public class CoinbaseService2 extends MarketService {

    static final TimestampBasedMarketServiceSupporter support = new TimestampBasedMarketServiceSupporter(10000, true, true);

    private static final DateTimeFormatter TimeFormat = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSSSS][.SSSSS][.SSSS][.SSS][.SS][.S]X");

    /** The API limit. */
    private static final APILimiter LIMITER = APILimiter.with.limit(3).refresh(1000, MILLISECONDS);

    /** The realtime communicator. */
    private final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://advanced-trade-ws.coinbase.com")
            .extractId(json -> json.text("channel"));

    /**
     * @param marketName
     * @param setting
     */
    protected CoinbaseService2(String marketName, MarketSetting setting) {
        super(Exchange.Coinbase2, marketName, setting);

        Realtime.subscribe(new Topic("heartbeats", marketName)).to(I.NoOP);
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
        long startSec = support.computeEpochSecond(startId);
        long endSec = support.computeEpochSecond(endId);
        long currentSec = endSec;
        long startMill = startSec * 1000;
        long[] currentMill = {currentSec * 1000};

        ArrayDeque<Execution> buffer = new ArrayDeque();
        while (startSec < currentSec) {
            query(startSec, currentSec).take(e -> startMill <= e.mills && e.mills < currentMill[0]).waitForTerminate().to(buffer::addFirst);

            Execution first = buffer.getFirst();
            currentSec = support.computeEpochSecond(first.id);
            currentMill[0] = first.mills;
        }
        return I.signal(buffer);
    }

    private Signal<Execution> query(long startTime, long endTime) {
        long[] context = new long[3];

        return call("GET", "market/products/" + marketName + "/ticker?limit=1000&start=" + startTime + "&end=" + endTime)
                .flatIterable(e -> e.find("trades", "*"))
                .plug(e -> createExecutionForREST(e, context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        long[] context = new long[3];

        return clientRealtimely().subscribe(new Topic("market_trades", marketName))
                .flatIterable(e -> e.find("events", "*", "trades", "$"))
                .map(e -> createExecution(e, context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "market/products/" + marketName + "/ticker?limit=15").flatIterable(e -> e.find("trades", "*"))
                .plug(e -> createExecutionForREST(e, new long[3]))
                .last();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsBefore(long id) {
        long[] context = new long[3];

        return call("GET", "market/products/" + marketName + "/ticker?limit=15").flatIterable(e -> e.find("trades", "*"))
                .plug(e -> createExecutionForREST(e, context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> searchInitialExecution() {
        return call("GET", "products/" + marketName + "/trades?after=2").flatIterable(e -> e.find("*"))
                .map(json -> createExecution(json, new long[3]));
    }

    /**
     * In the REST API, the following events can occur.
     * - IDs at the same time are not in the same order.
     * - The side is “UNKNOWN_ORDER_SIDE”.
     * - A trade_id that is too small is mixed in.
     * Cannot happen in WESOCKET API.
     * 
     * @param json
     * @param previous
     * @return
     */
    private Signal<Execution> createExecutionForREST(Signal<JSON> signal, long[] previous) {
        return signal.map(e -> createExecution(e, previous)).skipNull();
    }

    /**
     * In the REST API, the following events can occur.
     * - IDs at the same time are not in the same order.
     * - The side is “UNKNOWN_ORDER_SIDE”.
     * - A trade_id that is too small is mixed in.
     * Cannot happen in WESOCKET API.
     * 
     * @param json
     * @param previous
     * @return
     */
    private Execution createExecution(JSON e, long[] previous) {
        String textSide = e.text("side");
        if (textSide.charAt(0) == 'U' /* UNKNOWN_ORDER_SIDE */) {
            // It seems OTC trade, so we should discard it.
            //
            // TradeID contains unintelligible formats such as
            // 2022-09-13T07:10:00.06Z-BTC/USD-22343.5-1.22
            return null;
        } else {
            Direction side = Direction.parse(textSide);
            Num size = Num.of(e.text("size"));
            Num price = Num.of(e.text("price"));
            ZonedDateTime date = ZonedDateTime.parse(e.text("time"), TimeFormat);

            return support.createExecution(side, size, price, date, previous);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookChanges> orderBook() {
        return call("GET", "products/" + marketName + "/book?level=2")
                .map(e -> OrderBookChanges.byJSON(e.find("bids", "*"), e.find("asks", "*"), "0", "1"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookChanges> connectOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("level2_50", marketName)).map(root -> {
            List<JSON> items = root.find("changes", "*");
            OrderBookChanges changes = OrderBookChanges.byHint(items.size());

            for (JSON item : items) {
                Direction side = item.get(Direction.class, "0");
                double price = Double.parseDouble(item.text("1"));
                float size = Float.parseFloat(item.text("2"));

                changes.add(side == Direction.BUY, price, size);
            }

            return changes;
        });
    }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JSON> call(String method, String path) {
        Builder builder = HttpRequest.newBuilder(URI.create("https://api.coinbase.com/api/v3/brokerage/" + path));

        return Network.rest(builder, LIMITER, client()).retry(withPolicy());
    }

    public static void main1(String[] args) throws InterruptedException {
        Coinbase2.BTCUSD.executionsRealtimely().to(e -> {
            System.out.println(e);
        });

        Thread.sleep(1000 * 500);
    }

    public static void main3(String[] args) throws InterruptedException {
        ZonedDateTime now = Chrono.utcNow().minusYears(2);
        ZonedDateTime start = now.minusMinutes(15);

        Coinbase2.BTCUSD.executions(support.computeID(start), support.computeID(now)).to(e -> {
            System.out.println(e);
        });

        Thread.sleep(1000 * 3);
    }

    public static void main(String[] args) throws InterruptedException {
        Coinbase2.BTCUSD.executions().to(e -> {
            System.out.println(e);
        });

        Thread.sleep(1000 * 500);
    }

    /**
     * Subscription topic for websocket communication.
     */
    static class Topic extends IdentifiableTopic<Topic> {

        public String type = "subscribe";

        public List<String> product_ids = new ArrayList();

        public String channel;

        private Topic(String channel, String market) {
            super((channel.equals("level2_50") ? "l2update" : channel));

            product_ids.add(market);
            this.channel = channel;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean verifySubscribedReply(JSON reply) {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void buildUnsubscribeMessage(Topic topic) {
            topic.type = "unsubscribe";
        }
    }
}