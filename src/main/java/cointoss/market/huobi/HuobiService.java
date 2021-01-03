/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.huobi;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.RandomStringUtils;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.market.Exchange;
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

public class HuobiService extends MarketService {

    /** The right padding for id. */
    private static final long PaddingForID = 100000;

    /** The realtime data format */
    private static final DateTimeFormatter RealTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    /** The bitflyer API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(10).refresh(Duration.ofSeconds(1));

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://api-aws.huobi.pro/ws")
            .extractId(json -> json.text("ch"))
            .pongIf(json -> {
                String id = json.text("ping");
                return id == null ? null : "{'pong':" + id + "}";
            });

    /**
     * @param marketName
     * @param setting
     */
    protected HuobiService(String marketName, MarketSetting setting) {
        super(Exchange.Huobi, marketName, setting);
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
        long startingPoint = startId % PaddingForID;
        AtomicLong increment = new AtomicLong(startingPoint - 1);
        Object[] previous = new Object[] {null, encodeId(startId)};

        return call("GET", "trade?symbol=" + marketName + "&count=1000" + "&startTime=" + formatEncodedId(startId) + "&start=" + startingPoint)
                .flatIterable(e -> e.find("*"))
                .map(json -> convert(json, increment, previous));
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
    protected Signal<Execution> connectExecutionRealtimely() {
        AtomicLong increment = new AtomicLong();
        Object[] previous = new Object[2];

        return clientRealtimely().subscribe(new Topic("trade.detail", marketName))
                .flatIterable(json -> json.find("tick", "data", "*"))
                .map(json -> convert(json, increment, previous));
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param json
     * @param previous
     * @return
     */
    private Execution convert(JSON e, AtomicLong increment, Object[] previous) {
        Direction side = e.get(Direction.class, "direction");
        Num size = e.get(Num.class, "amount");
        Num price = e.get(Num.class, "price");
        ZonedDateTime date = Chrono.utcByMills(e.get(long.class, "ts"));
        long id = e.get(long.class, "tradeId");
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
     * Convert to {@link Execution}.
     * 
     * @param json
     * @param previous
     * @return
     */
    private Execution convert2(JSON e, AtomicLong increment, Object[] previous) {
        Direction side = e.get(Direction.class, "direction");
        Num size = e.get(Num.class, "amount");
        Num price = e.get(Num.class, "price");
        ZonedDateTime date = Chrono.utcByMills(e.get(long.class, "ts"));
        long id = e.get(long.class, "trade-id");
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
    public Signal<Execution> executionLatest() {
        return call("GET", "market/trade?symbol=" + marketName).flatIterable(e -> e.find("tick", "data", "*"))
                .map(json -> convert2(json, new AtomicLong(), new Object[2]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsBefore(long id) {
        return call("GET", "market/history/trade?symbol=" + marketName + "&size=2000&page=20")
                .flatIterable(e -> e.find("data", "*", "data", "*"))
                .map(json -> convert2(json, new AtomicLong(), new Object[2]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookPageChanges> orderBook() {
        return I.signal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> connectOrderBookRealtimely() {
        return I.signal();
    }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JSON> call(String method, String path) {
        Builder builder = HttpRequest.newBuilder(URI.create("https://api.huobi.pro/" + path));

        return Network.rest(builder, Limit, client()).retryWhen(retryPolicy(10, "Huobi RESTCall"));
    }

    /**
     * Subscription topic for websocket communication.
     */
    static class Topic extends IdentifiableTopic<Topic> {

        public String sub;

        public String unsub;

        public String id = RandomStringUtils.randomAlphabetic(6);

        private Topic(String channel, String market) {
            super("market." + market + "." + channel);

            this.sub = "market." + market + "." + channel;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean verifySubscribedReply(JSON reply) {
            return id.equals(reply.text("id")) && "ok".equals(reply.text("status"));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void buildUnsubscribeMessage(Topic topic) {
            topic.unsub = topic.sub;
            topic.sub = null;
        }
    }
}