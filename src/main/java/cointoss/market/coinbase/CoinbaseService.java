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

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.market.Exchange;
import cointoss.market.TimestampBasedMarketServiceSupporter;
import cointoss.orderbook.OrderBookChanges;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import cointoss.util.RateLimiter;
import hypatia.Num;
import kiss.I;
import kiss.JSON;
import kiss.Signal;

public class CoinbaseService extends MarketService {

    static final TimestampBasedMarketServiceSupporter support = new TimestampBasedMarketServiceSupporter();

    private static final DateTimeFormatter TimeFormat = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSSSS][.SSSSS][.SSSS][.SSS][.SS][.S]X");

    /** The API limit. */
    private static final RateLimiter LIMIT = RateLimiter.with.limit(10).refreshSecond(1).persistable(Exchange.Coinbase);

    /** The realtime communicator. */
    private final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://advanced-trade-ws.coinbase.com")
            .extractId(json -> json.text("channel"));

    /**
     * @param marketName
     * @param setting
     */
    protected CoinbaseService(String marketName, MarketSetting setting) {
        super(Exchange.Coinbase, marketName, setting);
        this.enoughExecutionRequest = true;
        this.executionMaxRequest = 1000;
        this.executionRequestCoefficient = 8;

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
    public Signal<Execution> executionsAfter(long startId, long endId) {
        /*
         * The expected ID range is equal to [modifier * acquirableExecutionSize].
         * The expected Time range is equal to [modifier * 1000 * 60 * 5 * support.padding]
         * The acquirableExecutionSize is 1000 in coinbase. So
         */
        long expectedIdRange = endId - startId;
        long expectedTimeRange = expectedIdRange * 60 * 5 * support.padding;

        long startSec = support.computeEpochSecond(startId) + 1;
        long endSec = support.computeEpochSecond(startId + expectedTimeRange);

        // Since only a maximum of 1,000 items can be retrieved in a single request, multiple
        // requests must be made to retrieve all the data in the specified range.
        long[] context = new long[3];
        Deque<List<Execution>> children = new ArrayDeque();

        while (startSec < endSec) {
            List<Execution> child = new ArrayList(1000);
            children.addFirst(child);

            call("GET", "market/products/" + marketName + "/ticker?limit=1000&start=" + startSec + "&end=" + endSec)
                    .flatIterable(e -> e.find("trades", "$"))
                    .map(e -> createExecution(e, true, context))
                    .waitForTerminate()
                    .to(child::add);

            if (child.size() < 1000) {
                // COMPLETED
                // The fact that only fewer than 1,000 data items have been retrieved despite a
                // request for 1000 data items means that all data after the specified time has
                // been retrieved.
                break;
            } else {
                // INCOMPLETED
                // The fact that the same number of data is retrieved after requesting 1000 data
                // items means that there is still data that has not been retrieved since the
                // specified time.

                // Note : child list still contains null generated by UNKNOWN_ORDER_SIDE. Consider
                // that the first element is null.
                for (int i = 0; i < 1000; i++) {
                    Execution item = child.get(i);
                    if (item != null) {
                        endSec = support.computeEpochSecond(item.id);
                        break;
                    }
                }
            }
        }
        return I.signal(children).flatIterable(x -> x).skipNull();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        long[] context = new long[3];

        return clientRealtimely().subscribe(new Topic("market_trades", marketName))
                .flatIterable(e -> e.find("events", "*", "trades", "$"))
                .map(e -> createExecution(e, false, context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "market/products/" + marketName + "/ticker?limit=15").flatIterable(e -> e.find("trades", "*"))
                .plug(e -> createExecutionForREST(e, new long[3]))
                .first();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsBefore(long id) {
        long startSec = support.computeEpochSecond(id);

        return call("GET", "market/products/" + marketName + "/ticker?limit=1000&end=" + startSec).flatIterable(e -> e.find("trades", "$"))
                .map(x -> createExecution(x, true, new long[3]));
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
        return signal.map(e -> createExecution(e, true, previous)).skipNull();
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
    private Execution createExecution(JSON e, boolean rest, long[] previous) {
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
        return call("GET", "market/product_book?product_id=" + marketName + "&limit=10000")
                .map(e -> OrderBookChanges.byJSON(e.find("bids", "*"), e.find("asks", "*"), "price", "size"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookChanges> connectOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("level2", marketName)).map(root -> {
            List<JSON> items = root.find("events", "*", "updates", "*");
            OrderBookChanges changes = OrderBookChanges.byHint(items.size());

            for (JSON item : items) {
                Direction side = Direction.parse(item.text("side"));
                double price = Double.parseDouble(item.text("price_level"));
                float size = Float.parseFloat(item.text("new_quantity"));

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

        return Network.rest(builder, LIMIT, client()).retry(withPolicy());
    }

    public static void main2(String[] args) throws InterruptedException {
        Coinbase.BTCUSD.executionsRealtimely().to(e -> {
        });

        Thread.sleep(1000 * 500);
    }

    public static void main1(String[] args) throws InterruptedException {
        Coinbase.BTCUSD.orderBookRealtimely().to(e -> {
            System.out.println(e);
        });

        Thread.sleep(1000 * 30);
    }

    public static void main(String[] args) throws InterruptedException {
        Coinbase.SOLUSD.executions().to(e -> {
        });
        Thread.sleep(1000 * 60 * 60 * 24);
    }

    /**
     * Subscription topic for websocket communication.
     */
    static class Topic extends IdentifiableTopic<Topic> {

        public String type = "subscribe";

        public List<String> product_ids = new ArrayList();

        public String channel;

        private Topic(String channel, String market) {
            super((channel.equals("level2") ? "l2_data" : channel));

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