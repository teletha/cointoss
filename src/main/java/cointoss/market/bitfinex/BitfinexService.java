/*
 * Copyright (C) 2019 CoinToss Development Team
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.execution.ExecutionLog;
import cointoss.order.Order;
import cointoss.order.OrderBookPage;
import cointoss.order.OrderBookPageChanges;
import cointoss.order.OrderState;
import cointoss.util.APILimiter;
import cointoss.util.Chrono;
import cointoss.util.Num;
import kiss.I;
import kiss.Signal;
import okhttp3.Request;

class BitfinexService extends MarketService {

    /** The right padding for id. */
    private static final long PaddingForID = 10000;

    /** The bitflyer API limit. */
    private static final APILimiter LimitForTradeHistory = APILimiter.with.limit(30).refresh(Duration.ofMinutes(1));

    /** The shared websocket connection. */
    private Signal<JsonElement> websocket;

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

        return call("GET", "hist?sort=1&limit=10000&start=" + startTime, LimitForTradeHistory).flatIterable(JsonElement::getAsJsonArray)
                .map(e -> convert(e.getAsJsonArray(), increment, previous));
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
        return call("GET", "hist?limit=1", LimitForTradeHistory).flatIterable(JsonElement::getAsJsonArray)
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
        return I.signal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> connectOrderBookRealtimely() {
        return I.signal();
    }

    /**
     * Convert json to {@link OrderBookPageChanges}.
     * 
     * @param array
     * @return
     */
    private OrderBookPageChanges convertOrderBook(JsonElement root, String bidName, String askName) {
        OrderBookPageChanges change = new OrderBookPageChanges();

        JsonObject o = root.getAsJsonObject();
        JsonArray bids = o.get(bidName).getAsJsonArray();
        JsonArray asks = o.get(askName).getAsJsonArray();

        for (JsonElement e : bids) {
            JsonArray bid = e.getAsJsonArray();
            Num price = Num.of(bid.get(0).getAsBigDecimal());
            double size = bid.get(1).getAsDouble();

            change.bids.add(new OrderBookPage(price, size));
        }

        for (JsonElement e : asks) {
            JsonArray ask = e.getAsJsonArray();
            Num price = Num.of(ask.get(0).getAsBigDecimal());
            double size = ask.get(1).getAsDouble();

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
    private Execution convert(JsonElement e, AtomicLong increment, Object[] previous) {
        JsonArray a = e.getAsJsonArray();
        ZonedDateTime date = Chrono.utcByMills(a.get(1).getAsLong());
        double size = a.get(2).getAsDouble();
        Num price = Num.of(a.get(3).getAsBigDecimal());
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
    private Signal<JsonElement> call(String method, String path, APILimiter limiter) {
        Request request = new Request.Builder().url("https://api-pub.bitfinex.com/v2/trades/t" + marketName + "/" + path).build();

        return network.rest(request, limiter).retryWhen(retryPolicy(10, "Bitfinex RESTCall"));
    }

    /**
     * Build shared websocket connection for this market.
     * 
     * @return
     */
    private synchronized Signal<JsonArray> connectSharedWebSocket(Topic topic) {
        String uri = "wss://api-pub.bitfinex.com/ws/2";

        if (websocket == null) {
            WebSocketCommand command = new WebSocketCommand();
            command.event = "subscribe";
            command.symbol = "t" + marketName;
            command.channel = topic.toString();

            websocket = network.websocket(uri, command);
        }

        MutableIntObjectMap<Topic> map = IntObjectMaps.mutable.empty();

        return websocket.share().flatMap(e -> {
            if (e.isJsonObject()) {
                JsonObject o = e.getAsJsonObject();
                if (o.has("channel")) {
                    String channel = o.get("channel").getAsString();
                    int channelId = o.get("chanId").getAsInt();
                    map.put(channelId, Topic.valueOf(channel));
                }
            } else {
                JsonArray arrat = e.getAsJsonArray();
                int id = arrat.get(0).getAsInt();
                Topic name = map.get(id);

                if (name == topic) {
                    JsonElement type = arrat.get(1);

                    // ignore snapshot and update
                    if (!type.isJsonArray() && type.getAsString().endsWith("e")) {
                        return I.signal(arrat.get(2)).as(JsonArray.class);
                    }
                }
            }
            return I.signal();
        });
    }

    /**
     * Subscription topics for websocket.
     */
    private enum Topic {
        trades(array -> {
            JsonElement type = array.get(1);

            // ignore snapshot and update
            if (!type.isJsonArray() && type.getAsString().endsWith("e")) {
                return I.signal(array.get(2)).as(JsonArray.class);
            } else {
                return I.signal();
            }
        });

        private final Function<JsonArray, Signal<JsonArray>> extractor;

        /**
         * @param extractor
         */
        private Topic(Function<JsonArray, Signal<JsonArray>> extractor) {
            this.extractor = extractor;
        }
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

    public static void main(String[] args) {

        // Bitfinex.BTC_USDT.executionsRealtimely().to(e -> {
        // System.out.println(e);
        // });
        new ExecutionLog(Bitfinex.BTC_USDT).fromYestaday().to(e -> {
        });
    }
}
