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
import cointoss.util.APILimiter;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import hypatia.Num;
import kiss.I;
import kiss.JSON;
import kiss.Signal;

public class HyperliquidService extends MarketService {

    static final TimestampBasedMarketServiceSupporter Support = new TimestampBasedMarketServiceSupporter();

    /** The bitflyer API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(1000).refresh(Duration.ofMinutes(1));

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://api.hyperliquid.xyz/ws").extractId(json -> {
        String channel = json.text("channel");
        return channel.equals("subscriptionResponse") ? "" : channel + "." + json.get("data").get("0").text("coin");
    });

    /**
     * @param marketName
     * @param setting
     */
    protected HyperliquidService(String marketName, MarketSetting setting) {
        super(Exchange.Hyperliquid, marketName, setting);
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
        System.out.println("ExecutionLatest");
        return convertCandle("1m", 0, Chrono.currentTimeMills()).last();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long startId, long endId) {
        long startTime = Support.computeEpochTime(startId);
        long currentTime = Chrono.currentTimeMills();
        long diff = (currentTime - startTime) / (1000 * 5000);
        if (diff <= 60) {
            return convertCandle("1m", startTime, currentTime);
        } else if (diff <= 60 * 5) {
            return convertCandle("5m", startTime, currentTime - (60L * 1 * 1000 * 5000));
        } else if (diff <= 60 * 15) {
            return convertCandle("15m", startTime, currentTime - (60L * 5 * 1000 * 5000));
        } else if (diff <= 60 * 30) {
            return convertCandle("30m", startTime, currentTime - (60L * 15 * 1000 * 5000));
        } else if (diff <= 60 * 60) {
            return convertCandle("1h", startTime, currentTime - (60L * 30 * 1000 * 5000));
        } else if (diff <= 60 * 240) {
            return convertCandle("4h", startTime, currentTime - (60L * 60 * 1000 * 5000));
        } else if (diff <= 60 * 480) {
            return convertCandle("8h", startTime, currentTime - (60L * 240 * 1000 * 5000));
        } else if (diff <= 60 * 720) {
            return convertCandle("12h", startTime, currentTime - (60L * 480 * 1000 * 5000));
        } else {
            return convertCandle("1d", startTime, currentTime - (60L * 720 * 1000 * 5000));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> searchInitialExecution() {
        System.out.println("SearchInitialExecution");
        return convertCandle("1d", 0, Chrono.currentTimeMills()).first().effect(e -> System.out.println(e));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsBefore(long id) {
        System.out.println("executionBefore " + Support.computeDateTime(id));
        return convertCandle("12h", 0, Support.computeEpochTime(id));
    }

    private Signal<Execution> convertCandle(String interval, long startMS, long endMS) {
        if (startMS >= endMS) {
            return I.signal();
        }

        System.out.println("Candle +" + interval + "+ start " + Chrono.utcByMills(startMS) + "   end " + Chrono.utcByMills(endMS));
        long intervalMillis = 1000 * switch (interval) {
        case "1m" -> 60;
        case "5m" -> 60 * 5;
        case "15m" -> 60 * 15;
        case "30m" -> 60 * 30;
        case "1h" -> 60 * 60;
        case "4h" -> 60 * 60 * 4;
        case "8h" -> 60 * 60 * 8;
        case "12h" -> 60 * 60 * 12;
        case "1d" -> 60 * 60 * 24;
        case "3d" -> 60 * 60 * 24 * 3;
        case "1w" -> 60 * 60 * 24 * 7;
        default -> throw new IllegalArgumentException("Unexpected value: " + interval);
        };

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
                """.formatted(marketName, interval, startMS, endMS)).flatIterable(json -> json.find("*")).flatIterable(json -> {
            Num open = json.get(Num.class, "o");
            Num close = json.get(Num.class, "c");
            Num high = json.get(Num.class, "h");
            Num low = json.get(Num.class, "l");
            Num volume = json.get(Num.class, "v");
            long time = json.get(long.class, "t");

            return Support.createExecutions(open, high, low, close, volume, time, intervalMillis);
        });
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

            return Support.createExecution(side, size, price, date, context);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookChanges> orderBook() {
        return I.signal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookChanges> connectOrderBookRealtimely() {
        return I.signal();
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