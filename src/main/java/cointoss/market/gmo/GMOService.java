/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.gmo;

import static java.util.concurrent.TimeUnit.*;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.execution.ExecutionLogRepository;
import cointoss.market.Exchange;
import cointoss.market.TimestampID;
import cointoss.order.Order;
import cointoss.order.OrderBookPage;
import cointoss.order.OrderBookPageChanges;
import cointoss.order.OrderState;
import cointoss.util.APILimiter;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.JSON;
import kiss.Signal;
import kiss.XML;

public class GMOService extends MarketService {

    /** The ID codec. */
    private static final TimestampID stamp = new TimestampID(true, 1000);

    /** The realtime data format */
    private static final DateTimeFormatter RealTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    /** The API limit. */
    private static final APILimiter LIMITER = APILimiter.with.limit(1).refresh(150, MILLISECONDS);

    /** The API limit. */
    private static final APILimiter REPOSITORY_LIMITER = APILimiter.with.limit(2).refresh(100, MILLISECONDS);

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://api.coin.z.com/ws/public/v1")
            .extractId(json -> json.text("channel") + "." + json.text("symbol"))
            .restrict(APILimiter.with.limit(1).refresh(2, SECONDS))
            .noServerReply();

    /**
     * @param marketName
     * @param setting
     */
    protected GMOService(String marketName, MarketSetting setting) {
        super(Exchange.GMO, marketName, setting);
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
    public Signal<String> request(Order order) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> cancel(Order order) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long startId, long endId) {
        ZonedDateTime start = stamp.decodeAsDate(startId);

        AtomicLong increment = new AtomicLong();
        Object[] prev = new Object[2];

        return I.signal(1)
                .recurse(i -> i + 1)
                .concatMap(page -> call("GET", "trades?symbol=" + marketName + "&page=" + page))
                .flatIterable(o -> o.find("data", "list", "*"))
                // The GMO server returns both Taker and Maker histories
                // alternately, so we have to remove the Maker side.
                .skipAt(index -> index % 2 == 0)
                .takeWhile(o -> ZonedDateTime.parse(o.text("timestamp"), RealTimeFormat).isAfter(start))
                .reverse()
                .map(e -> convert(e, increment, prev));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        AtomicLong increment = new AtomicLong();
        Object[] previous = new Object[2];

        return clientRealtimely().subscribe(new Topic("trades", marketName)).map(json -> convert(json, increment, previous));
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param json
     * @param previous
     * @return
     */
    private Execution convert(JSON e, AtomicLong increment, Object[] previous) {
        Direction side = e.get(Direction.class, "side");
        Num size = e.get(Num.class, "size");
        Num price = e.get(Num.class, "price");
        ZonedDateTime date = ZonedDateTime.parse(e.text("timestamp"), RealTimeFormat);

        return convert(side, size, price, date, increment, previous);
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param json
     * @param previous
     * @return
     */
    private Execution convert(Direction side, Num size, Num price, ZonedDateTime date, AtomicLong increment, Object[] previous) {
        long id;
        int consecutive;

        if (date.equals(previous[1])) {
            id = stamp.encode(date) + increment.incrementAndGet();

            if (side != previous[0]) {
                consecutive = Execution.ConsecutiveDifference;
            } else if (side == Direction.BUY) {
                consecutive = Execution.ConsecutiveSameBuyer;
            } else {
                consecutive = Execution.ConsecutiveSameSeller;
            }
        } else {
            id = stamp.encode(date);
            increment.set(0);
            consecutive = Execution.ConsecutiveDifference;
        }

        previous[0] = side;
        previous[1] = date;

        return Execution.with.direction(side, size).price(price).date(date).id(id).consecutive(consecutive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "trades?symbol=" + marketName + "&page=1&count=1").flatIterable(e -> e.find("data", "list", "*"))
                .map(json -> convert(json, new AtomicLong(), new Object[2]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatestAt(long id) {
        System.out.println(id);
        return I.signal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long estimateInitialExecutionId() {
        ExecutionLogRepository repo = externalRepository();

        return repo.collect().first().flatMap(repo::convert).first().map(e -> e.id).waitForTerminate().to().exact();
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
        return call("GET", "orderbooks?symbol=" + marketName).map(e -> convertOrderBook(e.get("data")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> connectOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("orderbooks", marketName)).map(this::convertOrderBook);
    }

    /**
     * Convert json to {@link OrderBookPageChanges}.
     * 
     * @param root
     * @return
     */
    private OrderBookPageChanges convertOrderBook(JSON root) {
        OrderBookPageChanges changes = new OrderBookPageChanges();
        changes.clearInside = true;

        for (JSON ask : root.find("asks", "*")) {
            Num price = ask.get(Num.class, "price");
            double size = ask.get(double.class, "size");
            changes.asks.add(new OrderBookPage(price, size));
        }
        for (JSON bid : root.find("bids", "*")) {
            Num price = bid.get(Num.class, "price");
            double size = bid.get(double.class, "size");
            changes.bids.add(new OrderBookPage(price, size));
        }

        return changes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> baseCurrency() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> targetCurrency() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutionLogRepository externalRepository() {
        return new OfficialRepository(this);
    }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JSON> call(String method, String path) {
        Builder builder = HttpRequest.newBuilder(URI.create("https://api.coin.z.com/public/v1/" + path));
        return Network.rest(builder, LIMITER, client()).flatMap(json -> {
            if (json.get(int.class, "status") != 0) {
                return I.signalError(new IllegalAccessError(json.get("messages").get("0").text("message_string")));
            } else {
                return I.signal(json);
            }
        }).retryWhen(retryPolicy(10, "GMO RESTCall"));
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
            super(channel + "." + market, topic -> topic.command = "unsubscribe");
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
            String uri = "https://api.coin.z.com/data/trades/" + service.marketName + "/";
            Function<Signal<XML>, Signal<String>> collect = s -> s.flatIterable(x -> x.find("ul li a")).map(XML::text);

            return I.http(uri, XML.class).plug(collect).flatMap(year -> {
                return I.http(uri + year + "/", XML.class).plug(collect).flatMap(month -> {
                    return I.http(uri + year + "/" + month + "/", XML.class).plug(collect).map(name -> {
                        return Chrono.utc(name.substring(0, name.indexOf("_")));
                    });
                });
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Execution> convert(ZonedDateTime date) {
            ZonedDateTime following = date.plusDays(1);

            AtomicLong increment = new AtomicLong();
            Object[] prev = new Object[2];
            DateTimeFormatter timeFormatOnLog = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm:ss.SSS");

            return downloadAt(date).concat(downloadAt(following)).map(values -> {
                Direction side = Direction.parse(values[1]);
                Num size = Num.of(values[2]);
                Num price = Num.of(values[3]);
                ZonedDateTime time = LocalDateTime.parse(values[4], timeFormatOnLog).atZone(Chrono.UTC);

                return GMOService.this.convert(side, size, price, time, increment, prev);
            }).skipUntil(e -> e.date.isAfter(date)).takeWhile(e -> e.date.isBefore(following));
        }

        /**
         * Download data and parse it as csv.
         * 
         * @param target
         * @return
         */
        private Signal<String[]> downloadAt(ZonedDateTime target) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy'/'MM'/'yyyyMMdd");
            String uri = "https://api.coin.z.com/data/trades/" + service.marketName + "/" + formatter
                    .format(target) + "_" + service.marketName + ".csv.gz";

            CsvParserSettings setting = new CsvParserSettings();
            setting.getFormat().setDelimiter(',');
            setting.getFormat().setLineSeparator("\n");
            setting.setHeaderExtractionEnabled(true);
            CsvParser parser = new CsvParser(setting);

            REPOSITORY_LIMITER.acquire();

            return I.http(uri, InputStream.class)
                    .errorResume(I.signal())
                    .flatIterable(in -> parser.iterate(new GZIPInputStream(in), StandardCharsets.ISO_8859_1))
                    .effectOnComplete(parser::stopParsing);
        }
    }
}