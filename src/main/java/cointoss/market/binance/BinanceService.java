/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.binance;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
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
import cointoss.market.TimestampBasedMarketServiceSupporter;
import cointoss.orderbook.OrderBookChanges;
import cointoss.ticker.Span;
import cointoss.ticker.data.Liquidation;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import cointoss.util.RateLimiter;
import hypatia.Num;
import kiss.I;
import kiss.JSON;
import kiss.Signal;

public class BinanceService extends MarketService {

    /** The execution support. */
    private static final TimestampBasedMarketServiceSupporter SUPPORT = new TimestampBasedMarketServiceSupporter();

    /** The API limit. */
    private static final RateLimiter Limit = RateLimiter.with.limit(900 /* 100 safe buffer */)
            .refreshSecond(10)
            .persistable(Exchange.Binance);

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://stream.binance.com:9443/stream")
            .extractId(json -> json.text("stream"));

    /**
     * The realtime communicator for the market data streams (e.g. trade) of the USDⓈ-M futures
     * market. Binance divides the futures websocket into several categories, and the market data
     * streams are served only under the {@code /market} path.
     */
    private static final EfficientWebSocket RealtimeFutureMarket = Realtime.withAddress("wss://fstream.binance.com/market/stream");

    /**
     * The realtime communicator for the public streams (e.g. order book) of the USDⓈ-M futures
     * market.
     */
    private static final EfficientWebSocket RealtimeFuturePublic = Realtime.withAddress("wss://fstream.binance.com/public/stream");

    /** The realtime communicator of the COIN-M futures market. */
    private static final EfficientWebSocket RealtimeDelivery = Realtime.withAddress("wss://dstream.binance.com/stream");

    private final boolean isDelivery;

    /**
     * @param marketName
     * @param setting
     */
    protected BinanceService(String marketName, MarketSetting setting) {
        super(setting.type.isDerivative() ? Exchange.BinanceF : Exchange.Binance, marketName, setting);

        this.executionMinRequest = 4;
        this.executionMaxRequest = 6000;
        this.isDelivery = setting.type.isDerivative() && setting.base.currency == Currency.USD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EfficientWebSocket clientRealtimely() {
        return setting.type.isSpot() ? Realtime : isDelivery ? RealtimeDelivery : RealtimeFutureMarket;
    }

    /**
     * Get the realtime websocket client for the public streams (e.g. order book). The USDⓈ-M
     * futures market serves its public streams under a path which differs from the market data
     * streams, so they can not share the communicator.
     * 
     * @return
     */
    protected EfficientWebSocket clientPublicRealtimely() {
        return setting.type.isSpot() || isDelivery ? clientRealtimely() : RealtimeFuturePublic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsAfter(long startId, long endId) {
        long startMS = SUPPORT.computeEpochTime(startId);

        // Generate executions from 1-minute klines. Four pseudo executions are generated from a
        // single candle, so request enough candles to satisfy the maximum number of executions,
        // bounded by the klines API limit (1500 = 6000 executions in a single request).
        long candles = Math.min(1500, Math.max(1, executionMaxRequest / 4));
        long endMS = startMS + Span.Minute1.duration.toMillis() * candles;

        return convertFromCandle(Span.Minute1, startMS, endMS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        long[] context = new long[3];

        return clientRealtimely().subscribe(new Topic("aggTrade", marketName)).map(json -> createExecution(json.get("data"), context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkEquality(Execution one, Execution other) {
        // Historical executions are generated from 1-minute candles, so their IDs are based on the
        // candle time and rarely match the realtime trade IDs exactly. Consider the REST API to have
        // caught up with the realtime API once the realtime execution reaches the historical one,
        // otherwise the handover from the historical data to the realtime data never happens.
        return other.id <= one.id;
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
    public Signal<Execution> executionsBefore(long id) {
        // Since aggTrades is restricted to the recent 2 days by ID, historical executions are
        // generated from 1-minute klines instead.
        long endMS = SUPPORT.computeEpochTime(id);

        // Four pseudo executions are generated from a single candle, so request enough candles to
        // satisfy the maximum number of executions, bounded by the klines API limit (1500).
        // The id is excluded afterwards, which may drop at most the last candle's executions.
        long candles = Math.min(1500, executionMaxRequest / 4);
        long startMS = Math.max(0, endMS - Span.Minute1.duration.toMillis() * candles);

        return convertFromCandle(Span.Minute1, startMS, endMS).take(e -> e.id < id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> searchInitialExecution() {
        return convertFromCandle(Span.Minute1, 0L, Chrono.currentTimeMills()).first();
    }

    /**
     * Convert from candle to pseudo {@link Execution}s.
     * 
     * @param span A candle span.
     * @param startMS A start time (epoch milliseconds).
     * @param endMS An end time (epoch milliseconds).
     * @return Pseudo executions.
     */
    private Signal<Execution> convertFromCandle(Span span, long startMS, long endMS) {
        if (startMS >= endMS) {
            return I.signal();
        }

        ZonedDateTime now = Chrono.utcNow();
        String path = setting.type.isSpot()
                ? "klines?symbol=" + marketName + "&interval=" + span.text + "&startTime=" + startMS + "&endTime=" + endMS + "&limit=1500"
                : "continuousKlines?pair=" + marketName + "&contractType=PERPETUAL&interval=" + span.text + "&startTime=" + startMS + "&endTime=" + endMS
                        + "&limit=1500";

        return call("GET", path, 20).flatIterable(root -> root.find("*")).flatIterable(json -> {
            Num open = json.get(Num.class, "1");
            Num high = json.get(Num.class, "2");
            Num low = json.get(Num.class, "3");
            Num close = json.get(Num.class, "4");
            Num volume = json.get(Num.class, "5");
            if (isDelivery) volume = volume.divide(close).scale(setting.target.scale);
            long time = json.get(long.class, "0");

            return SUPPORT.createExecutions(open, high, low, close, volume, time, span);
        }).skip(e -> e.isAfter(now));
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param e
     * @param context
     * @return
     */
    private Execution createExecution(JSON e, long[] context) {
        Direction side = e.get(Boolean.class, "m") ? Direction.SELL : Direction.BUY;
        Num size = e.get(Num.class, "q");
        Num price = e.get(Num.class, "p");
        if (isDelivery) size = size.divide(price).scale(setting.target.scale);
        ZonedDateTime date = Chrono.utcByMills(Long.parseLong(e.text("T")));

        return SUPPORT.createExecution(side, size, price, date, context);
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
        return clientPublicRealtimely().subscribe(new Topic("depth" + (setting.type.isSpot() ? "" : "@500ms"), marketName))
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

    // /**
    // * {@inheritDoc}
    // */
    // @Override
    // protected FeatherStore<OpenInterest> initializeOpenInterest() {
    // return FeatherStore.create(OpenInterest.class, Span.Minute5)
    // .enableInterpolation(4)
    // .enableDiskStore(file("oi.db"))
    // .enableDataSupplier(seconds -> {
    // // https://binance-docs.github.io/apidocs/futures/en/#open-interest-statistics
    // // [[ Only the data of the latest 30 days is available. ]]
    // // >>
    // // Contrary to the description in the document, setting the lower limit to 30
    // // days ago returns an error, so we set the lower limit to 25 days ago with a
    // // margin of 5 days.
    // ZonedDateTime lowerLimit = Chrono.utcNow().minusDays(25);
    // ZonedDateTime time = Chrono.max(lowerLimit, Chrono.utcBySeconds(seconds));
    //
    // return retrieveOpenInterest(time, 500);
    // }, I.schedule(LocalTime.of(0, 1), 5, MINUTES).flatMap(x -> {
    // return retrieveOpenInterest(Chrono.utcNow().minusMinutes(5 * 9), 10);
    // }));
    // }
    //
    // /**
    // * Call REST API for OI.
    // *
    // * @param start A starting time.
    // * @param size A total size to retrieve.
    // * @return
    // */
    // private Signal<OpenInterest> retrieveOpenInterest(ZonedDateTime time, int size) {
    // long start = time.toInstant().toEpochMilli();
    // long end = time.plusMinutes(5 * (size - 1)).toInstant().toEpochMilli();
    //
    // return call("GET", "https://fapi.binance.com/futures/data/openInterestHist?symbol=" +
    // marketName + "&period=5m&limit=" + size + "&startTime=" + start + "&endTime=" + end)
    // .flatIterable(e -> e.find("*"))
    // .map(e -> OpenInterest.with.date(Chrono.utcByMills(e.get(long.class, "timestamp")))
    // .size(e.get(float.class, "sumOpenInterest")));
    // }

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
                            .orientation(e.get(Direction.class, "side").inverse())
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
                    .orientation(json.get(Direction.class, "S").inverse())
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

        return Network.rest(builder, Limit, weight, client()).retry(withPolicy());
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