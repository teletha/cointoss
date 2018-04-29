/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitflyer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javafx.scene.control.TextInputDialog;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.RandomStringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cointoss.Execution;
import cointoss.MarketService;
import cointoss.Position;
import cointoss.Side;
import cointoss.order.Order;
import cointoss.order.Order.State;
import cointoss.order.OrderBookListChange;
import cointoss.order.OrderUnit;
import cointoss.util.Chrono;
import cointoss.util.Network;
import cointoss.util.Num;
import filer.Filer;
import kiss.Disposable;
import kiss.I;
import kiss.JSON;
import kiss.Signal;
import kiss.Signaling;
import marionette.Browser;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import viewtify.Viewtify;

/**
 * @version 2018/04/29 17:28:17
 */
public class BitFlyerService extends MarketService {

    /** The key for internal id. */
    private static final String InternalID = BitFlyerService.class.getName() + "#ID";

    private static final MediaType mime = MediaType.parse("application/json; charset=utf-8");

    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-");

    /** The realtime data format */
    private static final DateTimeFormatter RealTimeExecutionFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /** The api url. */
    static final String api = "https://api.bitflyer.jp";

    /** UTC */
    static final ZoneId zone = ZoneId.of("UTC");

    /** The order management. */
    private final Set<String> orders = ConcurrentHashMap.newKeySet();

    /** The position event. */
    private final Signaling<Position> positions = new Signaling();

    /** The market type. */
    private BitFlyer type;

    /** The key. */
    private final String accessKey;

    /** The token. */
    private final String accessToken;

    /** The key. */
    private final String name;

    /** The token. */
    private final String password;

    /** The token. */
    private final String accountId;

    private Disposable disposer = Disposable.empty();

    /** The shared order list. */
    private final Signal<List<Order>> intervalOrderCheck;

    /** The singleton. */
    private final OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).build();

    /** The session key. */
    private final String sessionKey = "api_session_v2";

    /**
     * @param type
     */
    BitFlyerService(BitFlyer type) {
        this.type = type;

        List<String> lines = Filer.read(".log/bitflyer/key.txt").toList();
        this.accessKey = lines.get(0);
        this.accessToken = lines.get(1);
        this.name = lines.get(2);
        this.password = lines.get(3);
        this.accountId = lines.get(4);
        this.intervalOrderCheck = I.signal(0, 1, TimeUnit.SECONDS).map(v -> orders().toList()).share();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
        disposer.dispose();
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<String> request(Order order) {
        Signal<String> call;
        String id = "JRF" + Chrono.utcNow().format(format) + RandomStringUtils.randomNumeric(6);

        if (maintainer.session() == null) {
            ChildOrderRequest request = new ChildOrderRequest();
            request.child_order_type = order.isLimit() ? "LIMIT" : "MARKET";
            request.minute_to_expire = 60 * 24;
            request.price = order.price.toInt();
            request.product_code = type.name();
            request.side = order.side().name();
            request.size = order.size.toDouble();
            request.time_in_force = order.quantity().abbreviation;

            call = call("POST", "/v1/me/sendchildorder", request, "child_order_acceptance_id", String.class);
        } else {
            ChildOrderRequestWebAPI request = new ChildOrderRequestWebAPI();
            request.account_id = accountId;
            request.ord_type = order.isLimit() ? "LIMIT" : "MARKET";
            request.minute_to_expire = 60 * 24;
            request.order_ref_id = id;
            request.price = order.price.toInt();
            request.product_code = type.name();
            request.side = order.side().name();
            request.size = order.size.toDouble();
            request.time_in_force = order.quantity().abbreviation;

            call = call("POST", "https://lightning.bitflyer.jp/api/trade/sendorder", request, "", WebResponse.class)
                    .map(e -> e.data.get("order_ref_id"));
        }

        return call.effect(v -> {
            // register order id
            orders.add(v);

            // check order state
            intervalOrderCheck.map(orders -> orders.get(orders.indexOf(order)))
                    .skipError()
                    .take(1)
                    .to(o -> order.attributes.putAll(o.attributes));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> cancel(Order order) {
        CancelRequest cancel = new CancelRequest();
        cancel.product_code = type.name();
        cancel.account_id = accountId;
        cancel.order_id = (String) order.attributes.get(InternalID);
        cancel.child_order_acceptance_id = order.id;

        Signal requestCancel = maintainer.session() == null || cancel.order_id == null
                ? call("POST", "/v1/me/cancelchildorder", cancel, null, null)
                : call("POST", "https://lightning.bitflyer.jp/api/trade/cancelorder", cancel, null, WebResponse.class);
        Signal<List<Order>> isCanceled = intervalOrderCheck.take(orders -> !orders.contains(order));

        return requestCancel.combine(isCanceled).take(1).mapTo(order).effect(o -> {
            orders.remove(order.id);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Integer> delay() {
        return Signal.NEVER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions() {
        return Network.websocket("wss://ws.lightstream.bitflyer.com/json-rpc", "lightning_executions_FX_BTC_JPY")
                .flatIterable(JsonElement::getAsJsonArray)
                .map(JsonElement::getAsJsonObject)
                .map(e -> {
                    BitFlyerExecution exe = new BitFlyerExecution();
                    exe.id = e.get("id").getAsLong();
                    exe.side = Side.parse(e.get("side").getAsString());
                    exe.price = Num.of(e.get("price").getAsString());
                    exe.size = exe.cumulativeSize = Num.of(e.get("size").getAsString());
                    exe.exec_date = LocalDateTime.parse(normalize(e.get("exec_date").getAsString()), RealTimeExecutionFormat)
                            .atZone(BitFlyerService.zone);
                    String buyer = exe.buy_child_order_acceptance_id = e.get("buy_child_order_acceptance_id").getAsString();
                    String seller = exe.sell_child_order_acceptance_id = e.get("sell_child_order_acceptance_id").getAsString();
                    exe.delay = estimateDelay(exe);

                    if (orders.contains(buyer)) {
                        Position position = new Position();
                        position.side = Side.BUY;
                        position.date = exe.exec_date;
                        position.price = exe.price;
                        position.size.set(exe.size);
                        position.id = buyer;

                        positions.accept(position);
                    } else if (orders.contains(seller)) {
                        Position position = new Position();
                        position.side = Side.SELL;
                        position.date = exe.exec_date;
                        position.price = exe.price;
                        position.size.set(exe.size);
                        position.id = seller;

                        positions.accept(position);
                    }

                    return exe;
                });
    }

    /**
     * Normalize time format.
     * 
     * @param time
     * @return
     */
    static String normalize(String time) {
        int size = time.length();

        // remove tail Z
        time = time.substring(0, size - 1);

        // padding 0
        if (size < 24) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 24 - size; i++) {
                builder.append("0");
            }
            time = time + builder;
        }
        return time.substring(0, 23);
    }

    private static final DateTimeFormatter TEST = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private static final ZoneOffset JST = ZoneOffset.ofHours(9);

    /**
     * <p>
     * Analyze Taker's order ID and obtain approximate order time (Since there is a bot which
     * specifies non-standard id format, ignore it in that case).
     * </p>
     * <ol>
     * <li>Execution Date : UTC</li>
     * <li>Server Order ID Date : UTC (i.e. stop-limit or IFD order)</li>
     * <li>User Order ID Date : JST+9:00</li>
     * </ol>
     *
     * @param exe
     * @return
     */
    public static long estimateDelay(Execution exe) {
        String taker = exe.side.isBuy() ? exe.buy_child_order_acceptance_id : exe.sell_child_order_acceptance_id;

        try {
            // order format is like the following [JRF20180427-123407-869661]
            // exclude illegal format
            if (taker == null || taker.length() != 25 || !taker.startsWith("JRF")) {
                return Execution.DelayUnknown;
            }

            // remove tail random numbers
            taker = taker.substring(3, 18);

            // parse as datetime
            long orderTime = LocalDateTime.parse(taker, TEST).toEpochSecond(JST);
            long executedTime = exe.exec_date.toEpochSecond() + 1;
            long diff = executedTime - orderTime;

            // estimate server order (over 9*60*60)
            if (32000 < diff) {
                return Execution.DelayServerOrder;
            } else if (diff < 0) {
                // some local client time is not stable, so ignore it
                return Execution.DelayInestimable;
            } else {
                return diff;
            }
        } catch (DateTimeParseException e) {
            return Execution.DelayUnknown;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long id) {
        return call("GET", "/v1/executions?product_code=" + type
                .name() + "&count=499&before=" + (id + 499), "", "*", BitFlyerExecution.class).as(Execution.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> orders() {
        return call("GET", "/v1/me/getchildorders?child_order_state=ACTIVE&product_code=" + type.name(), "", "*", ChildOrderResponse.class)
                .map(ChildOrderResponse::toOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Position> positions() {
        return positions.expose;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> getBaseCurrency() {
        return call("GET", "/v1/me/getbalance", "", "*", CurrencyState.class).take(unit -> unit.currency_code.equals("JPY"))
                .map(c -> c.available);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> getTargetCurrency() {
        return call("GET", "/v1/me/getbalance", "", "*", CurrencyState.class).take(unit -> unit.currency_code.equals("BTC"))
                .map(c -> c.available);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookListChange> getOrderBook() {
        return snapshotOrderBook().merge(realtimeOrderBook());
    }

    /**
     * Snapshot order book info.
     * 
     * @return
     */
    private Signal<OrderBookListChange> snapshotOrderBook() {
        return call("GET", "/v1/board?product_code=" + type, "", "", OrderBookListChange.class);
    }

    /**
     * Realtime order book info.
     * 
     * @return
     */
    private Signal<OrderBookListChange> realtimeOrderBook() {
        return Network.websocket("wss://ws.lightstream.bitflyer.com/json-rpc", "lightning_board_" + type)
                .map(JsonElement::getAsJsonObject)
                .map(e -> {
                    OrderBookListChange change = new OrderBookListChange();
                    JsonArray asks = e.get("asks").getAsJsonArray();

                    for (int i = 0; i < asks.size(); i++) {
                        JsonObject ask = asks.get(i).getAsJsonObject();
                        change.asks.add(new OrderUnit(Num.of(ask.get("price").getAsString()), Num.of(ask.get("size").getAsString())));
                    }

                    JsonArray bids = e.get("bids").getAsJsonArray();

                    for (int i = 0; i < bids.size(); i++) {
                        JsonObject bid = bids.get(i).getAsJsonObject();
                        change.bids.add(new OrderUnit(Num.of(bid.get("price").getAsString()), Num.of(bid.get("size").getAsString())));
                    }
                    return change;
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Execution decode(String[] values, Execution previous) {

        if (previous == null) {
            return new Execution(values);
        } else {
            Execution current = new Execution();
            current.id = previous.id + decode(values[0], 1);
            current.exec_date = previous.exec_date.plus(decode(values[1], 0), ChronoUnit.MILLIS);
            current.side = decode(values[2], previous.side);
            current.price = decode(values[3], previous.price);
            current.size = decodeSize(values[4], previous.size);
            current.buy_child_order_acceptance_id = String.valueOf(decode(values[5], previous.buyer()));
            current.sell_child_order_acceptance_id = String.valueOf(decode(values[6], previous.seller()));

            return current;
        }
    }

    private static long decode(String value, long defaults) {
        if (value == null || value.isEmpty()) {
            return defaults;
        }
        return Long.parseLong(value);
    }

    private static Side decode(String value, Side defaults) {
        if (value == null || value.isEmpty()) {
            return defaults;
        }
        return Side.parse(value);
    }

    private static Num decode(String value, Num defaults) {
        if (value == null || value.isEmpty()) {
            return defaults;
        }
        return Num.of(value).plus(defaults);
    }

    private static Num decodeSize(String value, Num defaults) {
        if (value == null || value.isEmpty()) {
            return defaults;
        }
        return Num.of(value).divide(Num.HUNDRED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] encode(Execution execution, Execution previous) {
        if (previous == null) {
            return execution.toString().split(" ");
        } else {
            String id = encode(execution.id, previous.id, 1);
            String time = encode(execution.exec_date, previous.exec_date);
            String side = encode(execution.side.mark(), previous.side.mark());
            String price = encode(execution.price, previous.price);
            String size = execution.size.equals(previous.size) ? "" : execution.size.multiply(Num.HUNDRED).toString();
            String consecutive = "1";
            String delay = String.valueOf(estimateDelay(execution));
            // String buyer = encode(execution.buyer(), previous.buyer(), 0);
            // String seller = encode(execution.seller(), previous.seller(), 0);

            return new String[] {id, time, side, price, size, consecutive + delay};
        }
    }

    /**
     * Erase duplicated value.
     * 
     * @param current
     * @param previous
     * @param defaults
     * @return
     */
    private static String encode(Num current, Num previous) {
        if (current.equals(previous)) {
            return "";
        } else {
            return current.minus(previous).toString();
        }
    }

    /**
     * Erase duplicated value.
     * 
     * @param current
     * @param previous
     * @param defaults
     * @return
     */
    private static String encode(long current, long previous, long defaults) {
        long diff = current - previous;

        if (diff == defaults) {
            return "";
        } else {
            return String.valueOf(diff);
        }
    }

    /**
     * Erase duplicated value.
     * 
     * @param current
     * @param previous
     * @param defaults
     * @return
     */
    private static String encode(ZonedDateTime current, ZonedDateTime previous) {
        Duration between = Duration.between(previous, current);

        if (between.isZero()) {
            return "";
        } else {
            return String.valueOf(between.toMillis());
        }
    }

    /**
     * Erase duplicated sequence.
     * 
     * @param current
     * @param previous
     * @return
     */
    private static String encode(String current, String previous) {
        return current.equals(previous) ? "" : current;
    }

    /**
     * Call private API.
     */
    private <M> Signal<M> call(String method, String path, Object body, String selector, Class<M> type) {
        StringBuilder builder = new StringBuilder();
        I.write(body, builder);

        return call(method, path, builder.toString(), selector, type);
    }

    /**
     * Call private API.
     */
    private <M> Signal<M> call(String method, String path, String body, String selector, Class<M> type) {
        return new Signal<>((observer, disposer) -> {
            String timestamp = String.valueOf(ZonedDateTime.now(zone).toEpochSecond());
            String sign = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, accessToken).hmacHex(timestamp + method + path + body);

            Request request;

            if (method.equals("GET")) {
                request = new Request.Builder().url(api + path)
                        .addHeader("ACCESS-KEY", accessKey)
                        .addHeader("ACCESS-TIMESTAMP", timestamp)
                        .addHeader("ACCESS-SIGN", sign)
                        .build();
            } else if (method.equals("POST") && !path.startsWith("http")) {
                request = new Request.Builder().url(api + path)
                        .addHeader("ACCESS-KEY", accessKey)
                        .addHeader("ACCESS-TIMESTAMP", timestamp)
                        .addHeader("ACCESS-SIGN", sign)
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(mime, body))
                        .build();
            } else {
                request = new Request.Builder().url(path)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Cookie", sessionKey + "=" + maintainer.session)
                        .addHeader("X-Requested-With", "XMLHttpRequest")
                        .post(RequestBody.create(mime, body))
                        .build();
            }

            try (Response response = client.newCall(request).execute()) {
                int code = response.code();
                String value = response.body().string();

                if (code == 200) {
                    if (type == null) {
                        observer.accept(null);
                    } else {
                        JSON json = I.json(value);

                        if (selector == null || selector.isEmpty()) {
                            M m = json.to(type);

                            if (m instanceof WebResponse) {
                                WebResponse res = (WebResponse) m;

                                if (res.error_message != null && !res.error_message.isEmpty()) {
                                    throw new Error(res.error_message);
                                }
                            }
                            observer.accept(m);
                        } else {
                            json.find(selector, type).to(observer);
                        }
                        observer.complete();
                    }
                } else {
                    observer.error(new Error("HTTP Status " + code + " " + value));
                }
            } catch (Throwable e) {
                observer.error(new Error("[" + path + "] throws some error.", e));
            }
            return disposer;
        });
    }

    private SessionMaintainer maintainer = new SessionMaintainer();

    /**
     * @version 2018/02/15 9:27:14
     */
    private class SessionMaintainer implements Disposable {

        /** The session id. */
        private String session;

        private boolean started = false;

        /**
         * Retrieve the session id.
         * 
         * @return
         */
        private String session() {
            if (session == null) {
                I.schedule(this::connect);
            }
            return session;
        }

        /**
         * Connect to server and get session id.
         */
        private synchronized void connect() {
            if (started == false) {
                started = true;

                try {
                    Browser browser = new Browser().configHeadless(false).configProfile(".log/bitflyer/chrome");
                    browser.load("https://lightning.bitflyer.jp") //
                            .input("#LoginId", name)
                            .input("#Password", password)
                            .click("#login_btn");

                    if (browser.uri().equals("https://lightning.bitflyer.jp/Home/TwoFactorAuth")) {
                        Viewtify.inUI(() -> {
                            String code = new TextInputDialog().showAndWait()
                                    .orElseThrow(() -> new IllegalArgumentException("二段階認証の確認コードが間違っています"))
                                    .trim();

                            Viewtify.inWorker(() -> {
                                browser.click("form > label").input("#ConfirmationCode", code).click("form > button");
                                session = browser.cookie(sessionKey);
                                browser.reload();
                                browser.dispose();
                            });
                        });
                    } else {
                        session = browser.cookie(sessionKey);
                        browser.reload();
                        browser.dispose();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw I.quiet(e);
                }
            }
        }

        /**
         * Maintain the session.
         */
        private void maintain() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void vandalize() {
        }
    }

    /**
     * @version 2017/11/13 13:09:00
     */
    @SuppressWarnings("unused")
    private static class ChildOrderRequest {

        public String product_code;

        public String child_order_type;

        public String side;

        public int price;

        public double size;

        public int minute_to_expire;

        public String time_in_force;
    }

    /**
     * @version 2018/02/14 13:36:32
     */
    private static class ChildOrderResponse {

        public Side side;

        public String child_order_id;

        public String child_order_acceptance_id;

        public Num size;

        public Num price;

        public Num average_price;

        public Num outstanding_size;

        public Num executed_size;

        public String child_order_date;

        public State child_order_state;

        public Order toOrder() {
            Order o = Order.limit(side, size, price);
            o.id = child_order_acceptance_id;
            o.averagePrice.set(average_price);
            o.remaining.set(outstanding_size);
            o.executed_size.set(executed_size);
            o.created.set(LocalDateTime.parse(child_order_date, Chrono.DateTimeWithT).atZone(Chrono.UTC));
            o.state.set(child_order_state);
            o.attributes.put(InternalID, child_order_id);

            return o;
        }
    }

    /**
     * @version 2018/01/29 1:23:05
     */
    @SuppressWarnings("unused")
    private static class ChildOrderRequestWebAPI {

        public String product_code;

        public String order_ref_id;

        public String ord_type;

        public String side;

        public int price;

        public double size;

        public double minute_to_expire;

        public String time_in_force;

        public String lang = "ja";

        public String account_id;
    }

    /**
     * @version 2018/02/23 16:41:56
     */
    @SuppressWarnings("unused")
    private static class CancelRequest {

        /** For REST API. */
        public String product_code;

        /** For REST API. */
        public String child_order_acceptance_id;

        /** For internal API. */
        public String lang = "ja";

        /** For internal API. */
        public String account_id;

        /** For internal API. */
        public String order_id;
    }

    /**
     * @version 2017/11/28 9:28:38
     */
    @SuppressWarnings("unused")
    private static class CurrencyState {

        /** The currency code. */
        public String currency_code;

        /** The total currency amount. */
        public Num amount;

        /** The available currency amount. */
        public Num available;
    }

    /**
     * @version 2018/02/09 11:42:27
     */
    @SuppressWarnings("unused")
    private class WebRequest {

        /** Generic parameter */
        public String account_id = accountId;

        /** Generic parameter */
        public String product_code = type.name();

        /** Generic parameter */
        public String lang = "ja";
    }

    /**
     * @version 2018/02/09 11:42:24
     */
    @SuppressWarnings("unused")
    private static class WebResponse {

        /** Generic parameter */
        public int status;

        /** Generic parameter */
        public String error_message;

        /** Generic parameter */
        public Map<String, String> data = new HashMap();

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "WebResponse [status=" + status + ", error_message=" + error_message + ", data=" + data + "]";
        }
    }

    /**
     * @version 2018/04/28 11:52:26
     */
    private static class BitFlyerExecution extends Execution {

    }
}
