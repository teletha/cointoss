/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.binance;

import static java.util.concurrent.TimeUnit.*;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cointoss.Currency;
import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.market.Exchange;
import cointoss.orderbook.OrderBookChanges;
import cointoss.ticker.Span;
import cointoss.ticker.data.Liquidation;
import cointoss.ticker.data.OpenInterest;
import cointoss.util.APILimiter;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import cointoss.util.arithmetic.Num;
import cointoss.util.feather.FeatherStore;
import kiss.I;
import kiss.JSON;
import kiss.Signal;

public class BinanceService extends MarketService {

    /** The API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(150 /* 50 safe buffer */).refresh(Duration.ofSeconds(5));

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://stream.binance.com:9443/stream")
            .extractId(json -> json.text("stream"));

    /** The realtime communicator. */
    private static final EfficientWebSocket RealtimeFuture = Realtime.withAddress("wss://fstream.binance.com/stream");

    /** The realtime communicator. */
    private static final EfficientWebSocket RealtimeDelivery = Realtime.withAddress("wss://dstream.binance.com/stream");

    private final boolean isDelivery;

    /**
     * @param marketName
     * @param setting
     */
    protected BinanceService(String marketName, MarketSetting setting) {
        super(setting.type.isDerivative() ? Exchange.BinanceF : Exchange.Binance, marketName, setting);

        this.isDelivery = setting.type.isDerivative() && setting.base.currency == Currency.USD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EfficientWebSocket clientRealtimely() {
        return setting.type.isSpot() ? Realtime : isDelivery ? RealtimeDelivery : RealtimeFuture;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long startId, long endId) {
        return call("GET", "aggTrades?symbol=" + marketName + "&limit=1000&fromId=" + (startId + 1), 20).flatIterable(e -> e.find("*"))
                .map(this::createExecution);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        return clientRealtimely().subscribe(new Topic("aggTrade", marketName)).map(json -> createExecution(json.get("data")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "aggTrades?symbol=" + marketName + "&limit=1", 20).flatIterable(e -> e.find("*")).map(this::createExecution);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsBefore(long id) {
        // Since the Binance API only provides a fromID parameter, subtract the ID by the maximum
        // number of acquisitions. If that makes the specified ID a negative number, emulate the API
        // by setting the ID to 0 and reduce the number of acquisitions.
        long limit = setting.acquirableExecutionSize;
        long fromId = id - limit;
        if (fromId < 0) {
            limit += fromId;
            fromId = 0;
        }

        return this.call("GET", "aggTrades?symbol=" + marketName + "&fromId=" + fromId + "&limit=" + limit, 20)
                .flatIterable(e -> e.find("*"))
                .map(this::createExecution);
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param e
     * @return
     */
    private Execution createExecution(JSON e) {
        long id = Long.parseLong(e.text("a"));
        Direction side = e.get(Boolean.class, "m") ? Direction.SELL : Direction.BUY;
        Num size = e.get(Num.class, "q");
        Num price = e.get(Num.class, "p");
        if (isDelivery) size = size.divide(price).scale(setting.target.scale);
        ZonedDateTime date = Chrono.utcByMills(Long.parseLong(e.text("T")));
        e.has("ok", true);

        return Execution.with.direction(side, size)
                .id(id)
                .price(price)
                .date(date)
                .consecutive(Execution.ConsecutiveDifference)
                .delay(Execution.DelayInestimable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookChanges> orderBook() {
        return call("GET", "depth?symbol=" + marketName + "&limit=" + (setting.type.isDerivative() ? "1000" : "1000"))
                .map(e -> createOrderBook(e, "bids", "asks"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookChanges> connectOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("depth" + (setting.type.isSpot() ? "" : "@500ms"), marketName))
                .map(json -> createOrderBook(json.get("data"), "b", "a"));
    }

    /**
     * Convert JSON to {@link OrderBookChanges}.
     * 
     * @param array
     * @return
     */
    private OrderBookChanges createOrderBook(JSON pages, String bidName, String askName) {
        return OrderBookChanges
                .byJSON(pages.find(bidName, "*"), pages.find(askName, "*"), "0", "1", isDelivery ? setting.target.scale : -1);
    }

    public static void main(String[] args) throws InterruptedException {
        Binance.LINK_USDT.log.fromLast(0).to(e -> {
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FeatherStore<OpenInterest> initializeOpenInterest() {
        return FeatherStore.create(OpenInterest.class, Span.Minute5)
                .enableInterpolation(4)
                .enableDiskStore(file("oi.db"))
                .enableDataSupplier(seconds -> {
                    // https://binance-docs.github.io/apidocs/futures/en/#open-interest-statistics
                    // [[ Only the data of the latest 30 days is available. ]]
                    // >>
                    // Contrary to the description in the document, setting the lower limit to 30
                    // days ago returns an error, so we set the lower limit to 25 days ago with a
                    // margin of 5 days.
                    ZonedDateTime lowerLimit = Chrono.utcNow().minusDays(25);
                    ZonedDateTime time = Chrono.max(lowerLimit, Chrono.utcBySeconds(seconds));

                    return retrieveOpenInterest(time, 500);
                }, I.schedule(LocalTime.of(0, 1), 5, MINUTES).flatMap(x -> {
                    return retrieveOpenInterest(Chrono.utcNow().minusMinutes(5 * 9), 10);
                }));
    }

    /**
     * Call REST API for OI.
     * 
     * @param start A starting time.
     * @param size A total size to retrieve.
     * @return
     */
    private Signal<OpenInterest> retrieveOpenInterest(ZonedDateTime time, int size) {
        long start = time.toInstant().toEpochMilli();
        long end = time.plusMinutes(5 * (size - 1)).toInstant().toEpochMilli();

        return call("GET", "https://fapi.binance.com/futures/data/openInterestHist?symbol=" + marketName + "&period=5m&limit=" + size + "&startTime=" + start + "&endTime=" + end)
                .flatIterable(e -> e.find("*"))
                .map(e -> OpenInterest.with.date(Chrono.utcByMills(e.get(long.class, "timestamp")))
                        .size(e.get(float.class, "sumOpenInterest")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Liquidation> liquidations(ZonedDateTime startExcluded, ZonedDateTime endExcluded) {
        if (setting.type.isSpot()) {
            return I.signal();
        }

        return call("GET", "allForceOrders?symbol=" + marketName + "&limit=1000&startTime=" + startExcluded.toInstant().toEpochMilli(), 20)
                .flatIterable(e -> e.find("*"))
                .map(e -> {
                    return Liquidation.with.date(Chrono.utcByMills(e.get(long.class, "time")))
                            .direction(e.get(Direction.class, "side").inverse())
                            .size(e.get(double.class, "executedQty"))
                            .price(e.get(Num.class, "averagePrice"));
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

        return clientRealtimely().subscribe(new Topic("forceOrder", marketName)).map(e -> {
            JSON json = e.get("data").get("o");
            return Liquidation.with.date(Chrono.utcByMills(json.get(long.class, "T")))
                    .direction(json.get(Direction.class, "S").inverse())
                    .size(json.get(double.class, "q"))
                    .price(json.get(Num.class, "ap"));
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
        return call(method, path, 1);
    }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JSON> call(String method, String path, int weight) {
        String uri = setting.type.isSpot() ? "https://api.binance.com/api/v3/"
                : isDelivery ? "https://dapi.binance.com/dapi/v1/" : "https://fapi.binance.com/fapi/v1/";
        Builder builder = HttpRequest.newBuilder(URI.create(path.startsWith("http") ? path : uri + path));

        return Network.rest(builder, Limit, weight, client()).retry(retryPolicy(retryMax, "Binance RESTCall"));
    }

    /**
     * 
     */
    static class Topic extends IdentifiableTopic<Topic> {

        private static final AtomicInteger counter = new AtomicInteger();

        public String method = "SUBSCRIBE";

        public List<String> params = new ArrayList();

        public int id = counter.incrementAndGet();

        /** The string expression to make equality checking fast. */
        private String idText = Integer.toString(id);

        private Topic(String channel, String market) {
            super(market.toLowerCase() + "@" + channel);
            this.params.add(market.toLowerCase() + "@" + channel);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean verifySubscribedReply(JSON reply) {
            return reply.has("id", idText);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void buildUnsubscribeMessage(Topic topic) {
            topic.method = "UNSUBSCRIBE";
        }
    }
}