/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.gmo;

import static java.util.concurrent.TimeUnit.*;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.execution.LogHouse;
import cointoss.market.Exchange;
import cointoss.market.TimestampBasedMarketServiceSupporter;
import cointoss.orderbook.OrderBookChanges;
import cointoss.util.APILimiter;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import cointoss.util.NetworkError.Kind;
import cointoss.util.NetworkErrorDetector;
import hypatia.Num;
import kiss.I;
import kiss.JSON;
import kiss.Signal;
import kiss.Variable;

public class GMOService extends MarketService {

    static final TimestampBasedMarketServiceSupporter Support = new TimestampBasedMarketServiceSupporter(1000);

    /** The realtime data format */
    private static final DateTimeFormatter RealTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    /** The error converter. */
    private static final NetworkErrorDetector ERRORS = new NetworkErrorDetector().register(Kind.LimitOverflow, "requests are too many")
            .register(Kind.Maintenance, "maintenance");

    /** The API limit. */
    private static final APILimiter LIMITER = APILimiter.with.limit(1).refresh(200, MILLISECONDS);

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://api.coin.z.com/ws/public/v1")
            .extractId(json -> json.text("channel") + "." + json.text("symbol"))
            .restrict(APILimiter.with.limit(1).refresh(1250, MILLISECONDS))
            .noServerReply();

    /**
     * @param marketName
     * @param setting
     */
    protected GMOService(String marketName, MarketSetting setting) {
        super(Exchange.GMO, marketName, setting);
        this.executionRequestLimit = 10000;
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
        ZonedDateTime start = Chrono.max(Support.computeDateTime(startId), ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(2));
        Variable<Long> counter = Variable.of(1L);
        long[] prev = new long[3];

        return counter.observing()
                .concatMap(page -> call("GET", "trades?symbol=" + marketName + "&page=" + page))
                .effect(() -> counter.set(v -> v + 1))
                .flatIterable(o -> o.find("data", "list", "*"))
                // The GMO server returns both Taker and Maker histories
                // alternately, so we have to remove the Maker side.
                .skipAt(index -> index % 2 == 0)
                .takeWhile(o -> ZonedDateTime.parse(o.text("timestamp"), RealTimeFormat).isAfter(start))
                .reverse()
                .map(e -> createExecution(e, prev));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        long[] previous = new long[3];

        return clientRealtimely().subscribe(new Topic("trades", marketName)).map(json -> createExecution(json, previous));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "trades?symbol=" + marketName + "&page=1&count=1").flatIterable(e -> e.find("data", "list", "*"))
                .map(json -> createExecution(json, new long[3]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsBefore(long id) {
        ZonedDateTime date = Support.computeDateTime(id);
        LogHouse repo = loghouse();

        return repo.convert(date).takeUntil(e -> id <= e.id).waitForTerminate();
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param json
     * @param previous
     * @return
     */
    private Execution createExecution(JSON e, long[] previous) {
        Direction side = e.get(Direction.class, "side");
        Num size = e.get(Num.class, "size");
        Num price = e.get(Num.class, "price");
        ZonedDateTime date = ZonedDateTime.parse(e.text("timestamp"), RealTimeFormat);

        return Support.createExecution(side, size, price, date, previous);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookChanges> orderBook() {
        return call("GET", "orderbooks?symbol=" + marketName).map(e -> createOrderBook(e.get("data")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookChanges> connectOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("orderbooks", marketName)).map(this::createOrderBook);
    }

    /**
     * Convert json to {@link OrderBookChanges}.
     * 
     * @param root
     * @return
     */
    private OrderBookChanges createOrderBook(JSON root) {
        return OrderBookChanges.byJSON(root.find("bids", "*"), root.find("asks", "*"), "price", "size");
    }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JSON> call(String method, String path) {
        Builder builder = HttpRequest.newBuilder(URI.create("https://api.coin.z.com/public/v1/" + path)).timeout(Duration.ofSeconds(60));
        return Network.rest(builder, LIMITER, client()).flatMap(json -> {
            if (json.get(int.class, "status") != 0) {
                String message = json.get("messages").get("0").text("message_string") + " [https://api.coin.z.com/public/v1/" + path + "]";
                return I.signalError(ERRORS.convert(message, this));
            } else {
                return I.signal(json);
            }
        }).retry(withPolicy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogHouse loghouse() {
        return new OfficialLogHouse(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportOrderBookFix() {
        return true;
    }

    public static void main(String[] args) throws InterruptedException {
        boolean has = GMO.BTC.loghouse().has(LocalDate.of(2024, 8, 3));
        System.out.println(has);
    }

    /**
     * Subscription topic for websocket communication.
     */
    static class Topic extends IdentifiableTopic<Topic> {

        public String command = "subscribe";

        public String channel;

        public String symbol;

        public String option = "TAKER_ONLY";

        private Topic(String channel, String market) {
            super(channel + "." + market);
            this.channel = channel;
            this.symbol = market;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean verifySubscribedReply(JSON reply) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void buildUnsubscribeMessage(Topic topic) {
            topic.command = "unsubscribe";
        }
    }
}