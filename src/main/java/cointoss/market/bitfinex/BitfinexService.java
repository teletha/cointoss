/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitfinex;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

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
import cointoss.util.Num;
import cointoss.util.SharedSocket;
import kiss.I;
import kiss.JSON;
import kiss.Signal;
import okhttp3.Request;

class BitfinexService extends MarketService {

    /** The right padding for id. */
    private static final long PaddingForID = 10000;

    /** The API limit. */
    private static final APILimiter LimitForTradeHistory = APILimiter.with.limit(30).refresh(Duration.ofMinutes(1));

    /** The API limit. */
    private static final APILimiter LimitForBook = APILimiter.with.limit(30).refresh(Duration.ofMinutes(1));

    /** The shared websocket connection. */
    private Signal<JSON> websocket;

    /**
     * @param marketName
     * @param setting
     */
    BitfinexService(String marketName, MarketSetting setting) {
        super("Bitfinex", marketName, setting);
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
        long startTime = (start / PaddingForID) + 1;
        long startingPoint = start % PaddingForID;
        AtomicLong increment = new AtomicLong(startingPoint - 1);
        Object[] previous = new Object[] {null, encodeId(start)};

        return call("GET", "trades/t" + marketName + "/hist?sort=1&limit=10000&start=" + startTime, LimitForTradeHistory)
                .flatIterable(e -> e.find("*"))
                .map(e -> convert(e, increment, previous));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        AtomicLong increment = new AtomicLong();
        Object[] previous = new Object[2];

        return connectSharedWebSocket(Topic.trades).map(e -> convert(e, increment, previous));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "trades/t" + marketName + "/hist?limit=1", LimitForTradeHistory).flatIterable(e -> e.find("*"))
                .map(e -> convert(e, new AtomicLong(), new Object[2]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long estimateInitialExecutionId() {
        return 15463008000000000L;
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
        return realtime.subscribe(Topic.book, marketName).map(json -> {
            OrderBookPageChanges change = new OrderBookPageChanges();
            JSON data = json.get("1");

            Num price = data.get(Num.class, "0");
            double size = data.get(Double.class, "2");

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
    private Execution convert(JSON a, AtomicLong increment, Object[] previous) {
        ZonedDateTime date = Chrono.utcByMills(a.get(Long.class, "1"));
        double size = a.get(Double.class, "2");
        Num price = a.get(Num.class, "3");
        Direction direction = 0 < size ? Direction.BUY : Direction.SELL;
        if (direction == Direction.SELL) size *= -1;

        long id;
        int consecutive;

        if (date.equals(previous[1])) {
            id = decodeId(date) + increment.incrementAndGet();

            if (direction != previous[0]) {
                consecutive = Execution.ConsecutiveDifference;
            } else if (direction == Direction.BUY) {
                consecutive = Execution.ConsecutiveSameBuyer;
            } else {
                consecutive = Execution.ConsecutiveSameSeller;
            }
        } else {
            id = decodeId(date);
            increment.set(0);
            consecutive = Execution.ConsecutiveDifference;
        }

        previous[0] = direction;
        previous[1] = date;

        return Execution.with.direction(direction, size).id(id).price(price).date(date).consecutive(consecutive);
    }

    private ZonedDateTime encodeId(long id) {
        return Chrono.utcByMills(id / PaddingForID);
    }

    private long decodeId(ZonedDateTime time) {
        return time.toInstant().toEpochMilli() * PaddingForID;
    }

    /**
     * Estimate consecutive type.
     * 
     * @param previous
     */
    private int estimateConsecutiveType(String prevBuyer, String prevSeller, String buyer, String seller) {
        if (buyer.equals(prevBuyer)) {
            if (seller.equals(prevSeller)) {
                return Execution.ConsecutiveSameBoth;
            } else {
                return Execution.ConsecutiveSameBuyer;
            }
        } else if (seller.equals(prevSeller)) {
            return Execution.ConsecutiveSameSeller;
        } else {
            return Execution.ConsecutiveDifference;
        }
    }

    /**
     * Analyze Taker's order ID and obtain approximate order time (Since there is a bot which
     * specifies non-standard id format, ignore it in that case).
     */
    private int estimateDelay(long eventTime, long tradeTime) {
        int delay = (int) (eventTime - tradeTime) / 1000;

        if (delay < 0) {
            return Execution.DelayInestimable;
        } else if (180 < delay) {
            return Execution.DelayHuge;
        } else {
            return delay;
        }
    }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JSON> call(String method, String path, APILimiter limiter) {
        Request request = new Request.Builder().url("https://api-pub.bitfinex.com/v2/" + path).build();

        return network.rest(request, limiter).retryWhen(retryPolicy(10, "Bitfinex RESTCall"));
    }

    /**
     * Build shared websocket connection for this market.
     * 
     * @return
     */
    private synchronized Signal<JSON> connectSharedWebSocket(Topic topic) {
        String uri = "wss://api-pub.bitfinex.com/ws/2";

        if (websocket == null) {
            WebSocketCommand command = new WebSocketCommand();
            command.event = "subscribe";
            command.symbol = "t" + marketName;
            command.channel = topic.toString();

            websocket = network.websocket(uri, command);
        }

        Map<Number, Topic> map = new HashMap();

        return websocket.share().flatMap(e -> {
            String channel = e.text("channel");

            if (channel != null) {
                map.put(e.get(Integer.class, "chanId"), Topic.valueOf(channel));
            } else {
                Topic name = map.get(e.get(Integer.class, "0"));
                if (name == topic) {
                    // ignore snapshot and update
                    if (e.text("1").endsWith("e")) {
                        return I.signal(e.get("2"));
                    }
                }
            }
            return I.signal();
        });
    }

    private final Realtime realtime = new Realtime();

    /**
     * Subscription topics for websocket.
     */
    private enum Topic {
        trades, book;
    }

    /**
     * 
     */
    @SuppressWarnings("unused")
    private static class WebSocketCommand {

        public String event;

        public String channel;

        public String symbol;
    }

    /**
     * 
     */
    private static class Realtime extends SharedSocket {

        /**
         *
         */
        private Realtime() {
            super("wss://api-pub.bitfinex.com/ws/2", I::json);
        }

        /**
         * Subscribe channel.
         * 
         * @param topic
         * @return
         */
        private Signal<JSON> subscribe(Topic topic, String symbol) {
            String[] id = {"-1"};

            // retrieve channel id
            expose.take(json -> json.has("event", "subscribed") && json.has("channel", topic.name()) && json.has("pair", symbol))
                    .first()
                    .to(json -> {
                        id[0] = json.text("chanId");
                    });

            return invoke(new Command("subscribe", topic.name(), symbol))
                    .effectOnDispose(() -> invoke(new Command("unsubscribe", topic.name(), symbol)))
                    .take(json -> json.has("0", id[0]) && !json.has("1", "hb")) // skip heartbeat
                    .skip(1); // skip snapshot
        }

        /**
         * 
         */
        private static class Command {

            public String event;

            public String channel;

            public String symbol;

            /**
             * @param channel
             * @param symbol
             */
            private Command(String event, String channel, String symbol) {
                this.event = event;
                this.channel = channel;
                this.symbol = symbol;
            }
        }
    }

    public static void main(String[] args) {

        // Bitfinex.BTC_USDT.executionLatest().to(e -> {
        // System.out.println(e);
        // });
        //
        // Bitfinex.BTC_USDT.executionsRealtimely().to(e -> {
        // System.out.println(e);
        // });

        Bitfinex.BTC_USDT.orderBookRealtimely().to(e -> {
            e.bids.forEach(page -> {
                System.out.println(page);
            });
            e.asks.forEach(page -> {
                System.out.println(page);
            });
        }, e -> {
            e.printStackTrace();
        }, () -> {
            System.out.println("COMPLETE");
        });
    }
}