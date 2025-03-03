/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.hyperliquid;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;
import java.time.ZonedDateTime;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.market.Exchange;
import cointoss.market.TimestampBasedMarketServiceSupporter;
import cointoss.orderbook.OrderBookChanges;
import cointoss.ticker.Span;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import cointoss.util.RateLimiter;
import hypatia.Num;
import kiss.I;
import kiss.JSON;
import kiss.Signal;

public class HyperliquidService extends MarketService {

    /** The execution support. */
    private static final TimestampBasedMarketServiceSupporter SUPPORT = new TimestampBasedMarketServiceSupporter();

    /** The bitflyer API limit. */
    private static final RateLimiter Limit = RateLimiter.with.limit(800).refresh(Duration.ofMinutes(1)).persistable(Exchange.Hyperliquid);

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://api.hyperliquid.xyz/ws").extractId(json -> {
        String channel = json.text("channel");

        // ignore
        if (channel.equals("subscriptionResponse")) {
            return "";
        }

        JSON data = json.get("data");

        // trades
        JSON trade = data.get("0");
        if (trade != null) {
            return channel + "." + trade.text("coin");
        }

        // l2book
        return channel + "." + data.text("coin");
    });

    protected HyperliquidService(String marketName, MarketSetting setting) {
        super(Exchange.Hyperliquid, marketName, setting);
        executionMinRequest = 4;
        executionMaxRequest = 5000;
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
    public Signal<Execution> executionLatest() {
        return SUPPORT.executionLatestByCandle(this::convertFromCandle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsAfter(long startId, long endId) {
        return SUPPORT.executionsAfterByCandle(startId, endId, executionMaxRequest, this::convertFromCandle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsBefore(long id) {
        return SUPPORT.executionsBeforeByCandle(id, this::convertFromCandle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> searchInitialExecution() {
        return SUPPORT.searchInitialExecutionByCandle(this::convertFromCandle);
    }

    /**
     * Convert from candle to pseudo executions.
     * 
     * @param span
     * @param startMS
     * @param endMS
     * @return
     */
    private Signal<Execution> convertFromCandle(Span span, long startMS, long endMS) {
        if (startMS >= endMS) {
            return I.signal();
        }

        ZonedDateTime now = Chrono.utcNow();
        return call(20, """
                {
                    "type": "candleSnapshot",
                    "req": {
                        "coin": "%s",
                        "interval": "%s",
                        "startTime": %d,
                        "endTime": %d
                    }
                }
                """.formatted(marketName, span.text, startMS, endMS)).flatIterable(json -> json.find("*")).flatIterable(json -> {
            Num open = json.get(Num.class, "o");
            Num close = json.get(Num.class, "c");
            Num high = json.get(Num.class, "h");
            Num low = json.get(Num.class, "l");
            Num volume = json.get(Num.class, "v");
            long time = json.get(long.class, "t");

            return SUPPORT.createExecutions(open, high, low, close, volume, time, span);
        }).skip(e -> e.isAfter(now));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        long[] context = new long[3];

        return clientRealtimely().subscribe(new Topic("trades", marketName)).flatIterable(json -> json.find("data", "*")).map(e -> {
            Direction side = Direction.parse(e.text("side"));
            Num price = e.get(Num.class, "px");
            Num size = e.get(Num.class, "sz");
            ZonedDateTime date = Chrono.utcByMills(e.get(long.class, "time"));

            return SUPPORT.createExecution(side, size, price, date, context);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookChanges> orderBook() {
        return call(2, """
                    {
                    "type": "l2Book",
                    "coin": "%s"
                }
                """.formatted(marketName)).map(pages -> {
            return OrderBookChanges.byJSON(pages.find("levels", "0", "*"), pages.find("levels", "1", "*"), "px", "sz");
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookChanges> connectOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("l2Book", marketName)).map(pages -> {
            return OrderBookChanges.byJSON(pages.find("data", "levels", "0", "*"), pages.find("data", "levels", "1", "*"), "px", "sz")
                    .full();
        });
    }

    public static void main(String[] args) throws InterruptedException {
        Hyperliquid.AI16Z.orderBookRealtimely().to(e -> {
            System.out.println(e.bestBid());
        });

        Thread.sleep(1000 * 40);
    }

    /**
     * Call rest API.
     * 
     * @param method
     * @param body
     * @return
     */
    private Signal<JSON> call(int weight, String body) {
        Builder builder = HttpRequest.newBuilder(URI.create("https://api.hyperliquid.xyz/info"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(body));

        return Network.rest(builder, Limit, weight, client()).retry(withPolicy());
    }

    static class Topic extends IdentifiableTopic<Topic> {

        public String method = "subscribe";

        public Subscription subscription;

        /**
         * @param channel
         * @param market
         */
        private Topic(String channel, String market) {
            super(channel + "." + market);

            subscription = new Subscription();
            subscription.type = channel;
            subscription.coin = market;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean verifySubscribedReply(JSON reply) {
            JSON json = reply.get("data").get("subscription");
            return reply.has("channel", "subscriptionResponse") && json.has("type", subscription.type) && json
                    .has("coin", subscription.coin);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void buildUnsubscribeMessage(Topic topic) {
            topic.method = "unsubscribe";
        }
    }

    static class Subscription {

        public String type;

        public String coin;

    }
}