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

    public static void main2(String... aa) throws InterruptedException {
        long start = 16072739833390013L;
        long diff = 10 * 1000 * Support.padding;
        Bybit.BTC_USD.executions(start, start + diff).waitForTerminate().to(e -> {
            System.out.println(e);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long startId, long endId) {
        return new Signal<Execution>((observer, disposer) -> {
            long id = search(Support.computeDateTime(startId)).map(e -> e.get(long.class, "id")).waitForTerminate().to().exact();
            int i = 0;
            long[] context = new long[3];

            while (!disposer.isDisposed()) {
                List<Execution> list = call("GET", "trading-records?symbol=" + marketName + "&limit=1000&from=" + (id + i++ * 1000))
                        .flatIterable(e -> e.find("result", "*"))
                        .map(e -> {
                            Direction side = e.get(Direction.class, "side");
                            Num price = e.get(Num.class, "price");
                            Num size = e.get(Num.class, "qty").divide(price).scale(setting.target.scale);
                            ZonedDateTime date = ZonedDateTime.parse(e.text("time"), TimeFormat);

                            return Support.createExecution(side, size, price, date, context);
                        })
                        .effect(observer::accept)
                        .waitForTerminate()
                        .toList();

                if (list.isEmpty() || 10 <= i) {
                    observer.complete();
                    break;
                }
            }
            return disposer;
        });
    }

    /**
     * Search by server ID.
     * 
     * @param target
     * @return
     */
    private Signal<JSON> search(ZonedDateTime target) {
        return call("GET", "trading-records?symbol=" + marketName + "&limit=1").flatIterable(e -> e.find("result", "*"))
                .concatMap(latest -> {
                    long id = latest.get(long.class, "id");
                    long time = time(latest);
                    return search(target.toInstant().toEpochMilli(), time, id, id - 1000);
                });
    }

    /**
     * Search by server ID.
     * 
     * @param targetTime
     * @param currentID
     * @param count
     * @return
     */
    private Signal<JSON> search(long targetTime, long baseTime, long baseID, long currentID) {
        return call("GET", "trading-records?symbol=" + marketName + "&limit=1000&from=" + currentID).concatMap(root -> {
            List<JSON> executions = root.find("result", "*");

            if (executions.isEmpty()) {
                return I.signal();
            }

            JSON first = executions.get(0);
            long firstTime = time(first);
            JSON last = executions.get(executions.size() - 1);
            long lastTime = time(last);

            if (firstTime <= targetTime && targetTime <= lastTime) {
                return I.signal(executions).skip(e -> time(e) < targetTime).first();
            } else {
                long firstID = first.get(long.class, "id");
                double timeDistance = baseTime - firstTime; // use double to avoid auto rounding
                long idDistance = baseID - firstID;
                double targetDistance = firstTime - targetTime; // use double to avoid auto rounding
                double mod = targetDistance / timeDistance;
                long diff = Math.round(idDistance * mod);
                long estimatedTargetID = diff != 0 ? firstID - diff : firstTime < targetTime ? firstID + 1000 : firstID - 1000;

                return search(targetTime, firstTime, firstID, estimatedTargetID);
            }
        }).first();
    }

    /**
     * Parse date-time.
     * 
     * @param json
     * @return
     */
    private long time(JSON json) {
        return ZonedDateTime.parse(json.text("time"), TimeFormat).toInstant().toEpochMilli();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        long[] context = new long[3];

        return clientRealtimely().subscribe(new Topic("trade", marketName)).flatIterable(json -> json.find("data", "*")).map(e -> {
            Direction side = e.get(Direction.class, "side");
            Num price = e.get(Num.class, "price");
            Num size = e.get(Num.class, "size").divide(price).scale(setting.target.scale);
            ZonedDateTime date = Chrono.utcByMills(Long.parseLong(e.text("trade_time_ms")));

            return Support.createExecution(side, size, price, date, context);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "trading-records?symbol=" + marketName + "&limit=1").flatIterable(e -> e.find("result", "*"))
                .map(json -> convert(json, new long[3]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsBefore(long id) {
        return call("GET", "trading-records?symbol=" + marketName + "&from=" + id).flatIterable(e -> e.find("result", "*"))
                .map(json -> convert(json, new long[3]));
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param e
     * @param context
     * @return
     */
    private Execution convert(JSON e, long[] context) {
        Direction side = e.get(Direction.class, "side");
        Num price = e.get(Num.class, "price");
        Num size = e.get(Num.class, "qty").divide(price).scale(setting.target.scale);
        ZonedDateTime date = ZonedDateTime.parse(e.text("time"), TimeFormat);

        return Support.createExecution(side, size, price, date, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkEquality(Execution one, Execution other) {
        return other.id <= one.id;
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