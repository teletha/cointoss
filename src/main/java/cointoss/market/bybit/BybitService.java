/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bybit;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
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
import kiss.Variable;
import kiss.XML;

public class BybitService extends MarketService {

    private static final TimestampBasedMarketServiceSupporter Support = new TimestampBasedMarketServiceSupporter();

    /** The realtime data format */
    private static final DateTimeFormatter TimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]X");

    /** The bitflyer API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(20).refresh(Duration.ofSeconds(1));

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://stream.bybit.com/realtime")
            .extractId(json -> json.text("topic"));

    /**
     * @param marketName
     * @param setting
     */
    protected BybitService(String marketName, MarketSetting setting) {
        super(Exchange.Bybit, marketName, setting);
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

        long[] id = {0};
        long[] context = new long[3];
        Variable<Long> page = Variable.of(1L);

        return page.observing() //
                .concatMap(i -> {
                    if (i == 1) {
                        return call("GET", "trading-records?symbol=" + marketName + "&limit=1000") //
                                .flatIterable(e -> e.find("result", "*"))
                                .effectOnce(e -> {
                                    id[0] = e.get(long.class, "id");
                                    page.set(v -> v + 1);
                                });
                    } else {
                        return call("GET", "trading-records?symbol=" + marketName + "&limit=1000&from=" + (id[0] - 1000 * i))
                                .effect(() -> page.set(v -> v + 1))
                                .flatIterable(e -> e.find("result", "*"))
                                .reverse();
                    }
                })
                .takeWhile(e -> ZonedDateTime.parse(e.text("time"), TimeFormat).isAfter(start))
                .reverse()
                .map(e -> {
                    Direction side = e.get(Direction.class, "side");
                    Num price = e.get(Num.class, "price");
                    Num size = e.get(Num.class, "qty").divide(price).scale(setting.target.scale);
                    ZonedDateTime date = ZonedDateTime.parse(e.text("time"), TimeFormat);

                    return Support.createExecution(side, size, price, date, context);
                });
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param e
     * @param previous
     * @return
     */
    private Execution convert(JSON e, Object[] previous) {
        Direction side = e.get(Direction.class, "side");
        Num price = e.get(Num.class, "price");
        Num size = e.get(Num.class, "qty").divide(price).scale(setting.target.scale);
        ZonedDateTime date = ZonedDateTime.parse(e.text("time"), TimeFormat);
        long id = Long.parseLong(e.text("id"));
        int consecutive;

        if (date.equals(previous[1])) {
            if (side != previous[0]) {
                consecutive = Execution.ConsecutiveDifference;
            } else if (side == Direction.BUY) {
                consecutive = Execution.ConsecutiveSameBuyer;
            } else {
                consecutive = Execution.ConsecutiveSameSeller;
            }
        } else {
            consecutive = Execution.ConsecutiveDifference;
        }

        previous[0] = side;
        previous[1] = date;

        return Execution.with.direction(side, size).id(id).price(price).date(date).consecutive(consecutive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        AtomicLong counter = new AtomicLong(-1);
        Object[] previous = new Object[2];

        return clientRealtimely().subscribe(new Topic("trade", marketName)).flatIterable(json -> json.find("data", "*")).map(e -> {
            long id = counter.updateAndGet(now -> now == -1 ? requestId(e) : now + 1);
            Direction side = e.get(Direction.class, "side");
            Num price = e.get(Num.class, "price");
            Num size = e.get(Num.class, "size").divide(price).scale(setting.target.scale);
            ZonedDateTime date = Chrono.utcByMills(Long.parseLong(e.text("trade_time_ms")));

            int consecutive;
            if (date.equals(previous[1])) {
                if (side != previous[0]) {
                    consecutive = Execution.ConsecutiveDifference;
                } else if (side == Direction.BUY) {
                    consecutive = Execution.ConsecutiveSameBuyer;
                } else {
                    consecutive = Execution.ConsecutiveSameSeller;
                }
            } else {
                consecutive = Execution.ConsecutiveDifference;
            }
            previous[0] = side;
            previous[1] = date;

            return Execution.with.direction(side, size).id(id).price(price).date(date).consecutive(consecutive);
        });
    }

    /**
     * Request the actual execution id.
     * 
     * @param exe The target execution data.
     * @return An actual id.
     */
    private synchronized long requestId(JSON exe) {
        String side = exe.text("side");
        String size = exe.text("size");
        String price = exe.text("price");
        long time = Long.parseLong(exe.text("trade_time_ms"));

        List<JSON> list = call("GET", "trading-records?symbol=" + marketName + "&limit=1000").waitForTerminate()
                .flatIterable(e -> e.find("result", "*"))
                .toList();

        for (JSON item : list) {
            if (!item.text("side").equals(side)) {
                continue;
            }

            if (!item.text("qty").equals(size)) {
                continue;
            }

            if (!item.text("price").equals(price)) {
                continue;
            }

            if (ZonedDateTime.parse(item.text("time"), TimeFormat).toInstant().toEpochMilli() != time) {
                continue;
            }
            return Long.parseLong(item.text("id"));
        }
        return Long.parseLong(list.get(0).text("id")) + 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "trading-records?symbol=" + marketName + "&limit=1").flatIterable(e -> e.find("result", "*"))
                .map(json -> convert(json, new Object[2]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsBefore(long id) {
        return call("GET", "trading-records?symbol=" + marketName + "&from=" + id).flatIterable(e -> e.find("result", "*"))
                .map(json -> convert(json, new Object[2]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookPageChanges> orderBook() {
        return call("GET", "orderBook/L2?symbol=" + marketName).map(pages -> {
            OrderBookPageChanges change = new OrderBookPageChanges();
            pages.find("result", "*").forEach(e -> convertOrderBook(change, e));
            return change;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> createOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("orderBook_200.100ms", marketName)).map(pages -> {
            OrderBookPageChanges change = new OrderBookPageChanges();

            String type = pages.text("type");
            if (type.equals("snapshot")) {
                pages.find("data", "*").forEach(e -> convertOrderBook(change, e));
            } else {
                pages.find("data", "delete", "*").forEach(e -> convertOrderBook(change, e));
                pages.find("data", "update", "*").forEach(e -> convertOrderBook(change, e));
                pages.find("data", "insert", "*").forEach(e -> convertOrderBook(change, e));
            }

            return change;
        });
    }

    /**
     * Convert to {@link OrderBookPage}.
     * 
     * @param changes
     * @param e
     */
    private void convertOrderBook(OrderBookPageChanges changes, JSON e) {
        Num price = e.get(Num.class, "price");
        String sizeValue = e.text("size");
        double size = sizeValue == null ? 0 : Double.parseDouble(sizeValue) / price.doubleValue();

        List<OrderBookPage> books = e.text("side").charAt(0) == 'B' ? changes.bids : changes.asks;
        books.add(new OrderBookPage(price, size));
    }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JSON> call(String method, String path) {
        Builder builder = HttpRequest.newBuilder(URI.create("https://api.bybit.com/v2/public/" + path));

        return Network.rest(builder, Limit, client()).retryWhen(retryPolicy(10, "Bybit RESTCall"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutionLogRepository externalRepository() {
        return new OfficialRepository(this);
    }

    /**
     * 
     */
    private static class OfficialRepository extends ExecutionLogRepository {

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
            String uri = "https://public.bybit.com/trading/" + service.marketName + "/";

            return I.http(uri, XML.class).flatIterable(o -> o.find("li a")).map(XML::text).map(name -> {
                return Chrono.utc(name.substring(service.marketName.length(), name.indexOf(".")));
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<Execution> convert(ZonedDateTime date) {
            String uri = "https://public.bybit.com/trading/" + service.marketName + "/" + service.marketName + Chrono.Date
                    .format(date) + ".csv.gz";

            CsvParserSettings setting = new CsvParserSettings();
            setting.getFormat().setDelimiter(',');
            setting.getFormat().setLineSeparator("\n");
            setting.setHeaderExtractionEnabled(true);
            CsvParser parser = new CsvParser(setting);

            long[] context = new long[3];

            return I.http(uri, InputStream.class)
                    .flatIterable(in -> parser.iterate(new GZIPInputStream(in), StandardCharsets.ISO_8859_1))
                    .effectOnComplete(parser::stopParsing)
                    .reverse()
                    .map(values -> {
                        ZonedDateTime time = parseTime(values[0]);
                        Direction side = Direction.parse(values[2]);
                        Num size = Num.of(values[9]);
                        Num price = Num.of(values[4]);

                        return Support.createExecution(side, size, price, time, context);
                    });
        }

        /**
         * Parse date-time expression.
         * 
         * @param dateTime
         * @return
         */
        private ZonedDateTime parseTime(String dateTime) {
            dateTime = dateTime.replace(".", "");

            long modifier;
            switch (16 - dateTime.length()) {
            case 1:
                modifier = 10;
                break;
            case 2:
                modifier = 100;
                break;
            case 3:
                modifier = 1000;
                break;
            case 4:
                modifier = 10000;
                break;
            case 5:
                modifier = 100000;
                break;
            case 6:
                modifier = 1000000;
                break;
            default:
                modifier = 1;
                break;
            }
            return Chrono.utcByMicros(Long.parseLong(dateTime) * modifier);
        }
    }

    /**
     * 
     */
    static class Topic extends IdentifiableTopic<Topic> {

        public String op = "subscribe";

        public List<String> args = new ArrayList();

        /**
         * @param channel
         * @param market
         */
        private Topic(String channel, String market) {
            super(channel + "." + market, topic -> topic.op = "unsubscribe");
            args.add(channel + "." + market);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean verifySubscribedReply(JSON reply) {
            if (reply.text("success").equals("true")) {
                JSON req = reply.get("request");
                if (req.text("op").equals("subscribe")) {
                    List<JSON> channel = req.find("args", "0");
                    if (channel.size() == 1) {
                        return channel.get(0).as(String.class).equals(args.get(0));
                    }
                }
            }
            return false;
        }
    }
}