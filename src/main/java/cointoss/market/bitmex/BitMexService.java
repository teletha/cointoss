/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitmex;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.market.Exchange;
import cointoss.market.TimestampBasedMarketServiceSupporter;
import cointoss.order.OrderBookPage;
import cointoss.order.OrderBookPageChanges;
import cointoss.ticker.data.Liquidation;
import cointoss.util.APILimiter;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import cointoss.util.arithmetic.Num;
import kiss.JSON;
import kiss.Signal;

public class BitMexService extends MarketService {

    private static final TimestampBasedMarketServiceSupporter Support = new TimestampBasedMarketServiceSupporter(100000);

    /** The realtime data format */
    private static final DateTimeFormatter RealTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    /** The bitflyer API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(45).refresh(Duration.ofMinutes(1));

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://www.bitmex.com/realtime")
            .extractId(json -> json.text("table") + json.find(String.class, "data", "0", "symbol"))
            .ignoreMessageIf(json -> json.has("info", "Welcome to the BitMEX Realtime API."));

    /** The market id. */
    private final int marketId;

    /** The instrument tick size. */
    private final Num instrumentTickSize;

    /**
     * @param marketName
     * @param setting
     */
    protected BitMexService(int id, String marketName, MarketSetting setting) {
        super(Exchange.BitMEX, marketName, setting);

        this.marketId = id;
        this.instrumentTickSize = marketName.equals("XBTUSD") ? Num.of("0.01") : setting.base.minimumSize;
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
        startId++;
        long startingPoint = startId % Support.padding;
        long[] previous = new long[] {0, 0, startingPoint - 1};

        return call("GET", "trade?symbol=" + marketName + "&count=1000" + "&startTime=" + formatEncodedId(startId) + "&start=" + startingPoint)
                .flatIterable(e -> e.find("*"))
                .map(json -> {
                    return convert(json, previous);
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        long[] previous = new long[3];

        return clientRealtimely().subscribe(new Topic("trade", marketName))
                .flatIterable(json -> json.find("data", "*"))
                .map(json -> convert(json, previous));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "trade?symbol=" + marketName + "&count=1&reverse=true").flatIterable(e -> e.find("*"))
                .map(json -> convert(json, new long[3]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsBefore(long id) {
        long[] context = new long[3];
        return call("GET", "trade?symbol=" + marketName + "&reverse=true&count=1000&endTime=" + formatEncodedId(id))
                .flatIterable(e -> e.find("$"))
                .map(json -> convert(json, context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkEquality(Execution one, Execution other) {
        return one.info.equals(other.info);
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param json
     * @param previous
     * @return
     */
    private Execution convert(JSON e, long[] previous) {
        Direction direction = Direction.parse(e.get(String.class, "side"));
        Num size = Num.of(e.get(String.class, "homeNotional"));
        Num price = Num.of(e.get(String.class, "price"));
        ZonedDateTime date = ZonedDateTime.parse(e.get(String.class, "timestamp"), RealTimeFormat).withZoneSameLocal(Chrono.UTC);
        String internalId = e.get(String.class, "trdMatchID");

        return Support.createExecution(direction, size, price, date, previous).assignInfo(internalId);
    }

    private String formatEncodedId(long id) {
        return RealTimeFormat.format(Support.computeDateTime(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookPageChanges> orderBook() {
        return call("GET", "orderBook/L2?depth=1200&symbol=" + marketName).map(e -> convertOrderBook(e.find("*")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> connectOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("orderBookL2", marketName))
                .map(json -> json.find("data", "*"))
                .map(this::convertOrderBook);
    }

    /**
     * Convert json to {@link OrderBookPageChanges}.
     * 
     * @param pages
     * @return
     */
    private OrderBookPageChanges convertOrderBook(List<JSON> pages) {
        OrderBookPageChanges change = new OrderBookPageChanges();
        for (JSON page : pages) {
            long id = Long.parseLong(page.text("id"));
            Num price = instrumentTickSize.multiply((100000000L * marketId) - id);
            JSON sizeElement = page.get("size");
            double size = sizeElement == null ? 0 : sizeElement.as(Double.class) / price.doubleValue();

            if (page.text("side").charAt(0) == 'B') {
                change.bids.add(new OrderBookPage(price, size));
            } else {
                change.asks.add(new OrderBookPage(price, size));
            }
        }
        return change;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Liquidation> connectLiquidation() {
        return clientRealtimely().subscribe(new Topic("liquidation", marketName))
                .take(e -> e.has("action", "insert"))
                .flatIterable(e -> e.find("data", "*"))
                .map(e -> {
                    double size = e.get(double.class, "leavesQty");
                    Num price = e.get(Num.class, "price");

                    return Liquidation.with.date(Chrono.utcNow())
                            .side(e.get(Direction.class, "side").inverse())
                            .size(price.divide(size).scale(setting.target.scale).doubleValue())
                            .price(price);
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
        Builder builder = HttpRequest.newBuilder(URI.create("https://www.bitmex.com/api/v1/" + path));

        return Network.rest(builder, Limit, client()).retryWhen(retryPolicy(10, "BitMEX RESTCall"));
    }

    /**
     * Subscription topic for websocket communication.
     */
    static class Topic extends IdentifiableTopic<Topic> {

        public String op = "subscribe";

        public List<String> args = new ArrayList();

        private final String id;

        private Topic(String channel, String market) {
            super(channel + "[" + market + "]", topic -> topic.op = "unsubscribe");

            this.id = channel + ":" + market;
            this.args.add(id);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean verifySubscribedReply(JSON reply) {
            return id.equals(reply.text("subscribe")) && Boolean.parseBoolean(reply.text("success"));
        }
    }
}