/*
 * Copyright (C) 2025 The COINTOSS Development Team
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
import cointoss.orderbook.OrderBookChanges;
import cointoss.ticker.data.Liquidation;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import cointoss.util.RateLimiter;
import hypatia.Num;
import kiss.JSON;
import kiss.Signal;

public class BitMexService extends MarketService {

    private static final TimestampBasedMarketServiceSupporter Support = new TimestampBasedMarketServiceSupporter(100000);

    /** The realtime data format */
    private static final DateTimeFormatter RealTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    /** The bitflyer API limit. */
    private static final RateLimiter LIMIT = RateLimiter.with.limit(30).refreshMinute(1).persistable(Exchange.BitMEX);

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://www.bitmex.com/realtime")
            .extractId(json -> json.text("table") + json.find(String.class, "data", "0", "symbol"))
            .stopRecconnectIf(json -> json.has("status", 400) && json.get(String.class, "error").startsWith("You are already subscribed"))
            .ignoreMessageIf(json -> json.has("info", "Welcome to the BitMEX Realtime API.") //
                    || (json.has("table", "liquidation") && json.has("action", "partial")));

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
        this.executionMaxRequest = 1000;
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
        startId++;
        long startingPoint = startId % Support.padding;
        long[] previous = new long[] {0, 0, startingPoint - 1};

        return call("GET", "trade?symbol=" + marketName + "&count=1000" + "&startTime=" + formatEncodedId(startId) + "&start=" + startingPoint)
                .flatIterable(e -> e.find("*"))
                .map(json -> convert(json, previous));
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
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> searchInitialExecution() {
        return call("GET", "trade?symbol=" + marketName + "&count=1&reverse=false").flatIterable(e -> e.find("*"))
                .map(json -> convert(json, new long[3]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> searchNearestExecution(ZonedDateTime target) {
        return call("GET", "trade?symbol=" + marketName + "&reverse=true&count=1&endTime=" + RealTimeFormat.format(target))
                .flatIterable(e -> e.find("$"))
                .map(json -> convert(json, new long[3]));
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
    public Signal<OrderBookChanges> orderBook() {
        return call("GET", "orderBook/L2?depth=1200&symbol=" + marketName).map(e -> convertOrderBook(e.find("*")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookChanges> connectOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("orderBookL2", marketName))
                .map(json -> json.find("data", "*"))
                .map(this::convertOrderBook);
    }

    /**
     * Convert json to {@link OrderBookChanges}.
     * 
     * @param items
     * @return
     */
    private OrderBookChanges convertOrderBook(List<JSON> items) {
        OrderBookChanges change = OrderBookChanges.byHint(items.size());
        for (JSON item : items) {
            long id = Long.parseLong(item.text("id"));
            double price = instrumentTickSize.doubleValue() * ((100000000L * marketId) - id);
            JSON sizeElement = item.get("size");
            float size = sizeElement == null ? 0 : sizeElement.as(Float.class) / (float) price;

            change.add(item.text("side").charAt(0) == 'B', price, size);
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
                            .orientation(e.get(Direction.class, "side").inverse())
                            .size(price.divide(size).scale(setting.target.scale).doubleValue())
                            .price(price);
                });
    }

    // /**
    // * {@inheritDoc}
    // */
    // @Override
    // protected FeatherStore<OpenInterest> initializeOpenInterest() {
    // return FeatherStore.create(OpenInterest.class, Span.Minute5)
    // .enableDiskStore(file("oi.db"))
    // .enablePassiveDataSupplier(connectOpenInterest());
    // }
    //
    // /**
    // * @return
    // */
    // private Signal<OpenInterest> connectOpenInterest() {
    // return clientRealtimely().subscribe(new Topic("instrument", marketName))
    // .take(e -> e.has("action", "update"))
    // .flatIterable(e -> e.find("data", "*"))
    // .take(e -> e.has("openInterest"))
    // .map(e -> {
    // ZonedDateTime time = ZonedDateTime.parse(e.text("timestamp"),
    // RealTimeFormat).truncatedTo(ChronoUnit.SECONDS);
    // float size = e.get(float.class, "openInterest");
    // float value = e.get(float.class, "openValue");
    // return OpenInterest.with.date(time).size(value / size);
    // });
    // }

    // public static void main(String[] args) throws InterruptedException {
    // double[] volume = new double[3];
    // double[] previousOISize = {0};
    //
    // BitMex.XBT_USD.executionsRealtimely().to(e -> {
    // if (e.isBuy()) {
    // volume[0] += e.size.doubleValue();
    // volume[2] += e.size.doubleValue() * e.price.doubleValue();
    // } else {
    // volume[1] += e.size.doubleValue();
    // volume[2] += e.size.doubleValue() * e.price.doubleValue();
    // }
    // });
    //
    // BitMex.XBT_USD.openInterestRealtimely().to(e -> {
    // double deltaOI = e.size - previousOISize[0];
    // double total = volume[0] + volume[1];
    // double entry = total + deltaOI / 2d;
    // double exit = total - deltaOI / 2d;
    //
    // System.out.println(e + " B:" + Primitives.roundString(volume[0], 6) + " S:" + Primitives
    // .roundString(volume[1], 6) + " Total:" + Primitives.roundString(volume[0] + volume[1], 6) + "
    // AvePrice:" + Primitives
    // .roundString(volume[2] / total, 2) + " Entry:" + Primitives
    // .roundString(entry, 2) + " Exit:" + Primitives.roundString(exit, 2));
    // volume[0] = 0;
    // volume[1] = 0;
    // volume[2] = 0;
    // previousOISize[0] = e.size;
    // });
    //
    // Thread.sleep(1000 * 60 * 10);
    // }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JSON> call(String method, String path) {
        Builder builder = HttpRequest.newBuilder(URI.create("https://www.bitmex.com/api/v1/" + path));

        return Network.rest(builder, LIMIT, client()).retry(withPolicy());
    }

    /**
     * Subscription topic for websocket communication.
     */
    static class Topic extends IdentifiableTopic<Topic> {

        public String op = "subscribe";

        public List<String> args = new ArrayList();

        private final String id;

        private Topic(String channel, String market) {
            super(channel + "[" + market + "]");

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

        /**
         * {@inheritDoc}
         */
        @Override
        protected void buildUnsubscribeMessage(Topic topic) {
            topic.op = "unsubscribe";
        }
    }
}