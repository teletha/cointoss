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

import static java.nio.charset.StandardCharsets.*;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.execution.ExecutionLog;
import cointoss.market.Exchange;
import cointoss.order.Order;
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

    /** The right padding for id. */
    private static final long PaddingForID = 1000;

    /** The realtime data format */
    private static final DateTimeFormatter RealTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    /** The bitflyer API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(45).refresh(Duration.ofMinutes(1));

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://api.coin.z.com/ws/public/v1")
            .extractId(json -> json.text("channel") + "." + json.text("symbol"))
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

    private ZonedDateTime encodeId(long id) {
        return Chrono.utcByMills(id / PaddingForID);
    }

    private String formatEncodedId(long id) {
        return RealTimeFormat.format(encodeId(id));
    }

    private long decodeId(ZonedDateTime time) {
        return time.toInstant().toEpochMilli() * PaddingForID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Integer> delay() {
        return null;
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
        ZonedDateTime start = encodeId(startId);
        ZonedDateTime end = encodeId(endId);

        return downloadHistoricalData(start).take(e -> Chrono.within(start, e.date, end)).effect(e -> System.out.println(e));
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
            id = decodeId(date) + increment.incrementAndGet();

            if (side != previous[0]) {
                consecutive = Execution.ConsecutiveDifference;
            } else if (side == Direction.BUY) {
                consecutive = Execution.ConsecutiveSameBuyer;
            } else {
                consecutive = Execution.ConsecutiveSameSeller;
            }
        } else {
            id = decodeId(date);
            increment.set(0);
            consecutive = Execution.ConsecutiveDifference;
        }

        previous[0] = side;
        previous[1] = date;

        return Execution.with.direction(side, size).price(price).date(date).id(id).consecutive(consecutive);
    }

    /**
     * Download data.
     * 
     * @param date
     * @return
     */
    private Signal<Execution> downloadHistoricalData(ZonedDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/yyyyMMdd");

        return downloadHistoricalData("https://api.coin.z.com/data/trades/" + marketName + "/" + formatter
                .format(date) + "_" + marketName + ".csv.gz");
    }

    /**
     * Download data.
     * 
     * @param uri
     * @return
     */
    private Signal<Execution> downloadHistoricalData(String uri) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm:ss.SSS");

        CsvParserSettings setting = new CsvParserSettings();
        setting.getFormat().setDelimiter(',');
        setting.getFormat().setLineSeparator("\n");
        setting.getFormat().setComment('無');
        setting.setHeaderExtractionEnabled(true);

        CsvParser parser = new CsvParser(setting);
        AtomicLong increment = new AtomicLong();
        Object[] prev = new Object[2];

        return I.http(uri, InputStream.class)
                .flatIterable(in -> parser.iterate(new GZIPInputStream(in), ISO_8859_1))
                .effectOnComplete(parser::stopParsing)
                .map(values -> {
                    Direction side = Direction.parse(values[1]);
                    Num size = Num.of(values[2]);
                    Num price = Num.of(values[3]);
                    ZonedDateTime time = LocalDateTime.parse(values[4], formatter).atZone(Chrono.UTC);

                    return convert(side, size, price, time, increment, prev);
                });
    }

    /**
     * Download all historical trade data from GMO server.
     */
    private void downloadAllHistoricalDataFromServer() {
        String uri = "https://api.coin.z.com/data/trades/" + marketName + "/";
        ExecutionLog log = new ExecutionLog(this);
        Function<Signal<XML>, Signal<String>> collect = s -> s.flatIterable(x -> x.find("ul li a")).map(XML::text).waitForTerminate();

        I.http(uri, XML.class).plug(collect).to(year -> {
            I.http(uri + year + "/", XML.class).plug(collect).to(month -> {
                I.http(uri + year + "/" + month + "/", XML.class).plug(collect).to(name -> {
                    ZonedDateTime date = Chrono.utc(name.substring(0, name.indexOf("_")));
                    log.storeFullDailyLog(date, downloadHistoricalData(date)
                            .effectOnComplete(() -> System.out.println("Download trade data. [" + marketName + " " + date + "]")));
                });
            });
        });

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "trades?symbol=" + marketName + "&page=1").effect(e -> System.out.println(e))
                .flatIterable(e -> e.find("data", "list", "*"))
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
        long[] id = {0};
        String uri = "https://api.coin.z.com/data/trades/" + marketName + "/";

        I.http(uri, XML.class).map(x -> x.find("ul li a").first().text()).waitForTerminate().to(year -> {
            I.http(uri + year + "/", XML.class).map(x -> x.find("ul li a").first().text()).waitForTerminate().to(month -> {
                I.http(uri + year + "/" + month + "/", XML.class).map(x -> x.find("ul li a").first().text()).waitForTerminate().to(name -> {
                    downloadHistoricalData(uri + year + "/" + month + "/" + name).first().waitForTerminate().to(values -> {
                        id[0] = values.id;
                    });
                });
            });
        });
        return id[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long estimateAcquirableExecutionIdRange(double factor) {
        return PaddingForID * 1000 * 60 * 60 * 24;
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
        return null;
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
    public boolean checkEquality(Execution one, Execution other) {
        return one.buyer.equals(other.buyer);
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

        return Network.rest(builder, Limit, client()).retryWhen(retryPolicy(10, "GMO RESTCall"));
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

    public static void main(String[] args) throws InterruptedException {
        downloadAllHistoricalData();
    }

    /**
     * Utility to download all trade data.
     */
    static void downloadAllHistoricalData() {
        I.signal(GMO.class.getDeclaredFields())
                .take(f -> f.getType() == MarketService.class)
                .map(f -> f.get(null))
                .as(GMOService.class)
                .to(GMOService::downloadAllHistoricalDataFromServer);
    }
}