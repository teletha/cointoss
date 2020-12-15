/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitbank;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.execution.ExecutionLogRepository;
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
import kiss.I;
import kiss.JSON;
import kiss.Signal;

public class BitbankService extends MarketService {

    private static final TimestampBasedMarketServiceSupporter Support = new TimestampBasedMarketServiceSupporter();

    /** The bitflyer API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(20).refresh(Duration.ofSeconds(1));

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://stream.bitbank.cc/socket.io/")
            .extractId(json -> json.text("room_name"))
            .enableSocketIO();

    /**
     * @param marketName
     * @param setting
     */
    protected BitbankService(String marketName, MarketSetting setting) {
        super(Exchange.BitBank, marketName, setting);
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
    public ExecutionLogRepository externalRepository() {
        return new OfficialRepository(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long startId, long endId) {
        long startMillis = Support.computeEpochTime(startId);
        ZonedDateTime today = Chrono.utcNow().minusMinutes(10).truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime startDay = Support.computeDateTime(startId).truncatedTo(ChronoUnit.DAYS);

        return I.signal(startDay)
                .recurse(day -> day.plusDays(1))
                .takeWhile(day -> day.isBefore(today) || day.isEqual(today))
                .sequenceMap(day -> {
                    if (day.isEqual(today)) {
                        return candleAt(day);
                    } else {
                        return executionsAt(day);
                    }
                })
                .skipUntil(e -> startMillis < e.mills)
                .take(1000);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        long[] previous = new long[3];

        return clientRealtimely().subscribe(new Topic("transactions", marketName))
                .flatIterable(json -> json.find("message", "data", "transactions", "*"))
                .map(e -> createExecution(e, previous));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", marketName + "/transactions").flatIterable(e -> e.find("data", "transactions", "*"))
                .first()
                .map(json -> createExecution(json, new long[3]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsBefore(long id) {
        throw new Error("No support.");
    }

    /**
     * Convert JSON to {@link Execution}.
     * 
     * @param json
     * @param context
     * @return
     */
    private Execution createExecution(JSON json, long[] context) {
        return Support.createExecution(json, "side", "amount", "price", "executed_at", context);
    }

    private Signal<Execution> executionsAt(ZonedDateTime date) {
        long[] previous = new long[3];

        return call("GET", marketName + "/transactions/" + Chrono.DateCompact.format(date))
                .flatIterable(e -> e.find("data", "transactions", "*"))
                .map(e -> createExecution(e, previous));
    }

    private Signal<Execution> candleAt(ZonedDateTime date) {
        return call("GET", marketName + "/candlestick/1min/" + Chrono.DateCompact.format(date))
                .flatIterable(e -> e.find("data", "candlestick", "0", "ohlcv", "*"))
                .flatIterable(e -> {
                    Num open = e.get(Num.class, "0");
                    Num high = e.get(Num.class, "1");
                    Num low = e.get(Num.class, "2");
                    Num close = e.get(Num.class, "3");
                    Num volume = e.get(Num.class, "4");
                    long epochMillis = e.get(long.class, "5");

                    return Support.createExecutions(open, high, low, close, volume, epochMillis);
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookPageChanges> orderBook() {
        return call("GET", marketName + "/depth").map(root -> {
            JSON data = root.get("data");

            OrderBookPageChanges change = new OrderBookPageChanges();
            for (JSON ask : data.find("asks", "*")) {
                change.asks.add(new OrderBookPage(ask.get(Num.class, "0"), ask.get(Double.class, "1")));
            }

            for (JSON bid : data.find("bids", "*")) {
                change.bids.add(new OrderBookPage(bid.get(Num.class, "0"), bid.get(Double.class, "1")));
            }
            return change;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> createOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("depth_diff", marketName)).map(root -> {
            JSON data = root.get("message").get("data");

            OrderBookPageChanges change = new OrderBookPageChanges();
            for (JSON ask : data.find("a", "*")) {
                change.asks.add(new OrderBookPage(ask.get(Num.class, "0"), ask.get(Double.class, "1")));
            }

            for (JSON bid : data.find("b", "*")) {
                change.bids.add(new OrderBookPage(bid.get(Num.class, "0"), bid.get(Double.class, "1")));
            }
            return change;
        });
    }

    /**
     * Call REST API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JSON> call(String method, String path) {
        Builder builder = HttpRequest.newBuilder(URI.create("https://public.bitbank.cc/" + path));

        return Network.rest(builder, Limit, client()).retryWhen(retryPolicy(10, "Bybit RESTCall"));
    }

    /**
     * 
     */
    static class Topic extends IdentifiableTopic<Topic> {

        /**
         * @param channel
         * @param market
         */
        private Topic(String channel, String market) {
            super(channel + "_" + market, topic -> {
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean verifySubscribedReply(JSON reply) {
            return false;
        }
    }

    /**
     * 
     */
    private class OfficialRepository extends ExecutionLogRepository {

        /**
         * @param service
         */
        private OfficialRepository(MarketService service) {
            super(service);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<ZonedDateTime> collect() {
            ZonedDateTime today = Chrono.utcToday();

            return I.signal(Chrono.utc(2018, 1, 1)).recurse(day -> day.plusDays(1)).takeWhile(day -> !day.isEqual(today));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Execution> convert(ZonedDateTime date) {
            return executionsAt(date);
        }
    }
}