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

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
import cointoss.market.TimestampBasedMarketServiceSupporter;
import cointoss.orderbook.OrderBookChanges;
import cointoss.util.APILimiter;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.JSON;
import kiss.Signal;
import kiss.Variable;
import kiss.XML;

public class GMOService extends MarketService {

    private static final TimestampBasedMarketServiceSupporter Support = new TimestampBasedMarketServiceSupporter(1000);

    /** The realtime data format */
    private static final DateTimeFormatter RealTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    /** The API limit. */
    private static final APILimiter LIMITER = APILimiter.with.limit(1).refresh(200, MILLISECONDS);

    /** The API limit. */
    private static final APILimiter REPOSITORY_LIMITER = APILimiter.with.limit(2).refresh(100, MILLISECONDS);

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
        ZonedDateTime start = Support.computeDateTime(startId);
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
        ExecutionLogRepository repo = externalRepository();

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

    public static void main2(String[] args) throws InterruptedException {
        GMO.BTC_DERIVATIVE.orderBookRealtimely(true).to(book -> {
            System.out.println(book);
        });

        Thread.sleep(1000 * 60 * 10);
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
                return I.signalError(new IllegalAccessError(json.get("messages").get("0").text("message_string") + " [" + path + "]"));
            } else {
                return I.signal(json);
            }
        }).retry(retryPolicy(retryMax, "GMO RESTCall"));
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
    public boolean supportOrderBookFix() {
        return true;
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

            long[] prev = new long[3];
            DateTimeFormatter timeFormatOnLog = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm:ss.SSS");

            return downloadAt(date).concat(downloadAt(following)).map(values -> {
                Direction side = Direction.parse(values[1]);
                Num size = Num.of(values[2]);
                Num price = Num.of(values[3]);
                ZonedDateTime time = LocalDateTime.parse(values[4], timeFormatOnLog).atZone(Chrono.UTC);

                return Support.createExecution(side, size, price, time, prev);
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
                    .stopError()
                    .flatIterable(in -> parser.iterate(new GZIPInputStream(in), StandardCharsets.ISO_8859_1))
                    .effectOnComplete(parser::stopParsing);
        }
    }
}