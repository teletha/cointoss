/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitfinex;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;
import java.time.ZonedDateTime;

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
import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.JSON;
import kiss.Signal;

public class BitfinexService extends MarketService {

    private static final TimestampBasedMarketServiceSupporter Support = new TimestampBasedMarketServiceSupporter();

    /** The API limit. */
    private static final APILimiter LimitForTradeHistory = APILimiter.with.limit(30).refresh(Duration.ofMinutes(1));

    /** The API limit. */
    private static final APILimiter LimitForBook = APILimiter.with.limit(30).refresh(Duration.ofMinutes(1));

    /** The realtiem communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://api-pub.bitfinex.com/ws/2")
            .extractId(json -> json.text("0"))
            .updateId(json -> json.text("chanId"))
            // ignore heartbeat and welcome message
            .ignoreMessageIf(json -> json.has("1", "hb") || json.has("event", "info"));

    /**
     * @param marketName
     * @param setting
     */
    protected BitfinexService(String marketName, MarketSetting setting) {
        super(Exchange.Bitfinex, marketName, setting);
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
        long startTime = Support.computeEpochTime(startId) + 1;
        long startingPoint = startId % Support.padding;
        long[] previous = new long[] {0, 0, startingPoint - 1};

        return call("GET", "trades/t" + marketName + "/hist?sort=1&limit=10000&start=" + startTime, LimitForTradeHistory)
                .flatIterable(e -> e.find("*"))
                .map(e -> createExecution(e, previous));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        long[] previous = new long[3];

        return clientRealtimely().subscribe(new Topic("trades", marketName))
                .take(e -> e.has("1", "te"))
                .map(e -> createExecution(e.get("2"), previous));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "trades/t" + marketName + "/hist?limit=1", LimitForTradeHistory).flatIterable(e -> e.find("*"))
                .map(e -> createExecution(e, new long[3]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsBefore(long id) {
        long startTime = Support.computeEpochTime(id) + 1;

        return call("GET", "trades/t" + marketName + "/hist?end=" + startTime + "&limit=" + setting.acquirableExecutionSize, LimitForTradeHistory)
                .flatIterable(e -> e.find("$"))
                .map(e -> createExecution(e, new long[3]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> searchInitialExecution() {
        return call("GET", "trades/t" + marketName + "/hist?sort=1&limit=1", LimitForTradeHistory).flatIterable(e -> e.find("*"))
                .map(e -> createExecution(e, new long[3]));
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param json
     * @param previous
     * @return
     */
    private Execution createExecution(JSON array, long[] previous) {
        ZonedDateTime date = Chrono.utcByMills(array.get(long.class, "1"));
        Num size = array.get(Num.class, "2");
        Num price = array.get(Num.class, "3");
        Direction side;
        if (size.isPositive()) {
            side = Direction.BUY;
        } else {
            side = Direction.SELL;
            size = size.negate();
        }

        return Support.createExecution(side, size, price, date, previous);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookPageChanges> orderBook() {
        return call("GET", "book/t" + marketName + "/P1?len=100", LimitForBook).map(json -> {
            OrderBookPageChanges change = new OrderBookPageChanges();

            for (JSON data : json.find("*")) {
                Num price = data.get(Num.class, "0");
                double size = data.get(Double.class, "2");

                if (0 < size) {
                    change.bids.add(new OrderBookPage(price, size));
                } else {
                    change.asks.add(new OrderBookPage(price, -size));
                }
            }
            return change;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> connectOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("book", marketName)).skip(1).map(json -> {
            OrderBookPageChanges change = new OrderBookPageChanges();
            JSON data = json.get("1");

            Num price = data.get(Num.class, "0");
            double size = Double.parseDouble(data.text("2"));

            if (0 < size) {
                change.bids.add(new OrderBookPage(price, size));
            } else {
                change.asks.add(new OrderBookPage(price, -size));
            }
            return change;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Liquidation> connectLiquidation() {
        if (setting.type.isSpot()) {
            return I.signal();
        }

        return clientRealtimely().subscribe(new Topic("status", "liq:global"))
                .skip(e -> e.text("1").equals("hb")) // ignore heartbeat
                .flatIterable(e -> e.find("1", "*"))
                .take(e -> e.text("4").contains(setting.target.currency.code) && e.has("8", 1))
                .map(e -> {
                    Num basePrice = e.get(Num.class, "6");
                    Num liquidatedPrice = e.get(Num.class, "11");
                    return Liquidation.with.date(Chrono.utcByMills(e.get(long.class, "2")))
                            .direction(basePrice.isLessThan(liquidatedPrice) ? Direction.BUY : Direction.SELL)
                            .size(Math.abs(e.get(double.class, "5")))
                            .price(liquidatedPrice);
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

        return clientRealtimely().subscribe(new Topic("status", "deriv:t" + marketName)).map(root -> {
            JSON e = root.get("1");
            return OpenInterest.with.date(Chrono.utcNow()).size(e.get(double.class, "17"));
        });
    }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JSON> call(String method, String path, APILimiter limiter) {
        Builder builder = HttpRequest.newBuilder(URI.create("https://api-pub.bitfinex.com/v2/" + path));

        return Network.rest(builder, limiter, client()).retryWhen(retryPolicy(10, "Bitfinex RESTCall"));
    }

    /**
     * 
     */
    static class Topic extends IdentifiableTopic<Topic> {

        public String event = "subscribe";

        public String channel;

        public int chanId;

        public String symbol;

        public String key;

        /** For book topic. */
        public String len = "250";

        private Topic(String channel, String symbol) {
            super(channel + symbol);
            this.channel = channel;
            this.symbol = symbol;
            this.key = symbol;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean verifySubscribedReply(JSON reply) {
            return "subscribed".equals(reply.text("event")) && channel
                    .equals(reply.text("channel")) && (symbol.equals(reply.text("pair")) || key.equals(reply.text("key")));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void buildUnsubscribeMessage(Topic topic) {
            topic.event = "unsubscribe";
            topic.chanId = Integer.parseInt(updatedId);
        }
    }
}