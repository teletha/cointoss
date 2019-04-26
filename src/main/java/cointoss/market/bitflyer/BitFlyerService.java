/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitflyer;

import static cointoss.order.OrderState.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.RandomStringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.order.OrderBookChange;
import cointoss.order.OrderState;
import cointoss.order.OrderUnit;
import cointoss.util.Chrono;
import cointoss.util.Num;
import javafx.scene.control.TextInputDialog;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import kiss.Signaling;
import kiss.Ⅲ;
import marionette.browser.Browser;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import viewtify.Viewtify;

/**
 * @version 2018/09/06 23:46:05
 */
class BitFlyerService extends MarketService {

    /** REUSE */
    private static final MediaType mime = MediaType.parse("application/json; charset=utf-8");

    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-");

    /** The realtime data format */
    static final DateTimeFormatter RealTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /** The realtime data format */
    static final DateTimeFormatter RealTimeFormatUntilSecond = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /** The realtime data format */
    static final DateTimeFormatter RealTimeFormatUntilMinute = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    /** The realtime data format */
    static final DateTimeFormatter RealTimeFormatUntilHour = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH");

    /** The api url. */
    static final String api = "https://api.bitflyer.com";

    /** The order management. */
    private final Set<String> orders = ConcurrentHashMap.newKeySet();

    /** The event stream of execution log for me. */
    private final Signaling<Ⅲ<Direction, String, Execution>> executionsForMe = new Signaling();

    /** Flag for test. */
    private final boolean forTest;

    /** The account setting. */
    private final BitFlyerAccount account = I.make(BitFlyerAccount.class);

    private Disposable disposer = Disposable.empty();

    /** The shared order list. */
    private final Signal<List<Order>> intervalOrderCheck;

    /** The session key. */
    private final String sessionKey = "api_session_v2";

    /** The latest realtime execution id. */
    private long latestId;

    /**
     * @param type
     */
    BitFlyerService(String type, MarketSetting.Builder setting) {
        this(type, false, setting);
    }

    /**
     * @param type
     */
    BitFlyerService(String type, boolean forTest, MarketSetting.Builder setting) {
        super("BitFlyer", type, setting.build());

        this.forTest = forTest;
        this.intervalOrderCheck = I.signal(0, 1, TimeUnit.SECONDS).map(v -> orders().toList()).share();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
        disposer.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<String> request(Order order) {
        Signal<String> call;
        String id = "JRF" + Chrono.utcNow().format(format) + RandomStringUtils.randomNumeric(6);

        if (forTest || maintainer.session() == null) {
            ChildOrderRequest request = new ChildOrderRequest();
            request.child_order_type = order.type.name();
            request.minute_to_expire = 60 * 24;
            request.price = order.price.toInt();
            request.product_code = marketName;
            request.side = order.direction().name();
            request.size = order.size.toDouble();
            request.time_in_force = order.condition.abbreviation;

            call = call("POST", "/v1/me/sendchildorder", request, "child_order_acceptance_id", String.class);
        } else {
            ChildOrderRequestWebAPI request = new ChildOrderRequestWebAPI();
            request.account_id = account.accountId.v;
            request.ord_type = order.type.name();
            request.minute_to_expire = 60 * 24;
            request.order_ref_id = id;
            request.price = order.price.toInt();
            request.product_code = marketName;
            request.side = order.direction().name();
            request.size = order.size.toDouble();
            request.time_in_force = order.condition.abbreviation;

            call = call("POST", "https://lightning.bitflyer.jp/api/trade/sendorder", request, "", WebResponse.class)
                    .map(e -> e.data.get("order_ref_id"));
        }

        return call.effectAfter(() -> {
            order.state.set(REQUESTING);
        }).effect(v -> {
            // register order id
            orders.add(v);
            order.observeTerminating().to(() -> orders.remove(v));

            // check order state
            intervalOrderCheck.takeUntil(order.observeTerminating())
                    .map(orders -> orders.get(orders.indexOf(order)))
                    .skipError()
                    .take(1)
                    .to(o -> {
                        order.state.set(ACTIVE);
                        order.relation(Internals.class).id = o.relation(Internals.class).id;
                    });
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> cancel(Order order) {
        CancelRequest cancel = new CancelRequest();
        cancel.product_code = marketName;
        cancel.account_id = account.accountId.v;
        cancel.order_id = order.relation(Internals.class).id;
        cancel.child_order_acceptance_id = order.id.v;

        Signal requestCancel = forTest || maintainer.session() == null || cancel.order_id == null
                ? call("POST", "/v1/me/cancelchildorder", cancel, null, null)
                : call("POST", "https://lightning.bitflyer.jp/api/trade/cancelorder", cancel, null, WebResponse.class);
        Signal<List<Order>> isCanceled = intervalOrderCheck.take(orders -> !orders.contains(order));

        return requestCancel.combine(isCanceled).take(1).mapTo(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Integer> delay() {
        return Signal.never();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsRealtimely() {
        return network.jsonRPC("wss://ws.lightstream.bitflyer.com/json-rpc", "lightning_executions_" + marketName)
                .flatIterable(JsonElement::getAsJsonArray)
                .map(JsonElement::getAsJsonObject)
                .map(e -> {
                    long id = e.get("id").getAsLong();

                    if (id == 0 && latestId == 0) {
                        return null; // skip
                    }

                    BitFlyerExecution exe = new BitFlyerExecution();
                    exe.id = latestId = id != 0 ? id : ++latestId;
                    exe.side = Direction.parse(e.get("side").getAsString());
                    exe.price = Num.of(e.get("price").getAsString());
                    exe.size = exe.cumulativeSize = Num.of(e.get("size").getAsString());
                    exe.exec_date = parse(e.get("exec_date").getAsString()).atZone(Chrono.UTC);
                    String buyer = exe.buy_child_order_acceptance_id = e.get("buy_child_order_acceptance_id").getAsString();
                    String seller = exe.sell_child_order_acceptance_id = e.get("sell_child_order_acceptance_id").getAsString();

                    if (orders.contains(buyer)) {
                        executionsForMe.accept(I.pair(Direction.BUY, buyer, exe));
                    } else if (orders.contains(seller)) {
                        executionsForMe.accept(I.pair(Direction.SELL, seller, exe));
                    }

                    return exe;
                })
                .skipNull()
                .maps(BitFlyerExecution.NONE, (prev, now) -> now.estimate(prev));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Ⅲ<Direction, String, Execution>> executionsRealtimelyForMe() {
        return executionsForMe.expose;
    }

    /**
     * Normalize time format.
     * 
     * @param time
     * @return
     */
    static LocalDateTime parse(String time) {
        int size = time.length();

        // remove tail Z
        time = time.substring(0, size - 1);

        switch (size) {
        case 14:
            return LocalDateTime.parse(time, RealTimeFormatUntilHour);

        case 17:
            return LocalDateTime.parse(time, RealTimeFormatUntilMinute);

        case 20:
            return LocalDateTime.parse(time, RealTimeFormatUntilSecond);

        default:
            // padding 0
            if (size < 24) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < 24 - size; i++) {
                    builder.append("0");
                }
                time = time + builder;
            }
            return LocalDateTime.parse(time.substring(0, 23), RealTimeFormat);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long start, long end) {
        return call("GET", "/v1/executions?product_code=" + marketName + "&count=" + setting
                .acquirableExecutionSize() + "&before=" + end + "&after=" + start, "", "^", BitFlyerExecution.class)
                        .maps(BitFlyerExecution.NONE, (prev, now) -> now.estimate(prev))
                        .as(Execution.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "/v1/executions?product_code=" + marketName + "&count=1", "", "^", BitFlyerExecution.class)
                .maps(BitFlyerExecution.NONE, (prev, now) -> now.estimate(prev))
                .as(Execution.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> orders() {
        return call("GET", "/v1/me/getchildorders?child_order_state=ACTIVE&product_code=" + marketName, "", "*", ChildOrderResponse.class)
                .map(ChildOrderResponse::toOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> baseCurrency() {
        return call("GET", "/v1/me/getbalance", "", "*", CurrencyState.class).take(unit -> unit.currency_code.equals("JPY"))
                .map(c -> c.available);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> targetCurrency() {
        return call("GET", "/v1/me/getbalance", "", "*", CurrencyState.class).take(unit -> unit.currency_code.equals("BTC"))
                .map(c -> c.available);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookChange> orderBook() {
        return snapshotOrderBook().concat(realtimeOrderBook());
    }

    /**
     * Snapshot order book info.
     * 
     * @return
     */
    private Signal<OrderBookChange> snapshotOrderBook() {
        return call("PUBLIC", "/v1/board?product_code=" + marketName, "", "", OrderBookChange.class);
    }

    /**
     * Realtime order book info.
     * 
     * @return
     */
    private Signal<OrderBookChange> realtimeOrderBook() {
        return network.jsonRPC("wss://ws.lightstream.bitflyer.com/json-rpc", "lightning_board_" + marketName)
                .map(JsonElement::getAsJsonObject)
                .map(e -> {
                    OrderBookChange change = new OrderBookChange();
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
    protected <M> Signal<M> call(String method, String path, String body, String selector, Class<M> type) {
        String timestamp = String.valueOf(Chrono.utcNow().toEpochSecond());
        String sign = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, account.apiSecret.v).hmacHex(timestamp + method + path + body);

        Request request;

        if (method.equals("PUBLIC")) {
            request = new Request.Builder().url(api + path).build();
        } else if (method.equals("GET")) {
            request = new Request.Builder().url(api + path)
                    .addHeader("ACCESS-KEY", account.apiKey.v)
                    .addHeader("ACCESS-TIMESTAMP", timestamp)
                    .addHeader("ACCESS-SIGN", sign)
                    .build();
        } else if (method.equals("POST") && !path.startsWith("http")) {
            request = new Request.Builder().url(api + path)
                    .addHeader("ACCESS-KEY", account.apiKey.v)
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
        return network.rest(request, selector, type);
    }

    protected SessionMaintainer maintainer = new SessionMaintainer();

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

                Browser browser = Browser.build(pref -> {
                    pref.profile(".log/bitflyer/chrome");
                });

                browser.load("https://lightning.bitflyer.jp") //
                        .input("#LoginId", account.loginId)
                        .input("#Password", account.loginPassword)
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
            }
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
    static class ChildOrderResponse {

        public Direction side;

        public String child_order_id;

        public String child_order_acceptance_id;

        public Num size;

        public Num price;

        public Num average_price;

        public Num outstanding_size;

        public Num executed_size;

        public String child_order_date;

        public OrderState child_order_state;

        public Order toOrder() {
            Order o = Order.of(side, size).price(price);
            o.id.let(child_order_acceptance_id);
            o.price(average_price);
            o.remainingSize.set(outstanding_size);
            o.executedSize.set(executed_size);
            o.creationTime.set(LocalDateTime.parse(child_order_date, Chrono.DateTimeWithT).atZone(Chrono.UTC));
            o.state.set(child_order_state);
            o.relation(Internals.class).id = child_order_id;

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
        public String account_id = account.accountId.v;

        /** Generic parameter */
        public String product_code = marketName;

        /** Generic parameter */
        public String lang = "ja";
    }

    /**
     * @version 2018/02/09 11:42:24
     */
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
     * @version 2018/05/25 9:37:29
     */
    private static class BitFlyerExecution extends Execution {

        /** The empty object. */
        private static final BitFlyerExecution NONE = new BitFlyerExecution();

        /** The bitflyer ID date fromat. */
        private static final DateTimeFormatter Format = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

        /** Buyer id of this execution. */
        public String buy_child_order_acceptance_id = "";

        /** Seller id of this execution. */
        public String sell_child_order_acceptance_id = "";

        /** The executed date-time. */
        public ZonedDateTime exec_date;

        /**
         * Estimate delay and consecutive type.
         * 
         * @param previous
         * @return
         */
        private BitFlyerExecution estimate(BitFlyerExecution previous) {
            this.date = this.exec_date;
            estimateConsecutiveType(previous);
            estimateDelay();

            return this;
        }

        /**
         * Estimate consecutive type.
         * 
         * @param previous
         */
        private void estimateConsecutiveType(BitFlyerExecution previous) {
            if (buy_child_order_acceptance_id.equals(previous.buy_child_order_acceptance_id)) {
                if (sell_child_order_acceptance_id.equals(previous.sell_child_order_acceptance_id)) {
                    consecutive = Execution.ConsecutiveSameBoth;
                } else {
                    consecutive = Execution.ConsecutiveSameBuyer;
                }
            } else if (sell_child_order_acceptance_id.equals(previous.sell_child_order_acceptance_id)) {
                consecutive = Execution.ConsecutiveSameSeller;
            } else {
                consecutive = Execution.ConsecutiveDifference;
            }
        }

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
        private void estimateDelay() {
            String taker = side.isBuy() ? buy_child_order_acceptance_id : sell_child_order_acceptance_id;

            try {
                // order format is like the following [JRF20180427-123407-869661]
                // exclude illegal format
                if (taker == null || taker.length() != 25 || !taker.startsWith("JRF")) {
                    delay = Execution.DelayInestimable;
                    return;
                }

                // remove tail random numbers
                taker = taker.substring(3, 18);

                // parse as datetime
                long orderTime = LocalDateTime.parse(taker, Format).toEpochSecond(ZoneOffset.UTC);
                long executedTime = exec_date.toEpochSecond() + 1;
                int diff = (int) (executedTime - orderTime);

                // estimate server order (over 9*60*60)
                if (diff < -32000) {
                    diff += 32400;
                }

                if (diff < 0) {
                    delay = Execution.DelayInestimable;
                } else if (180 < diff) {
                    delay = Execution.DelayHuge;
                } else {
                    delay = diff;
                }
            } catch (DateTimeParseException e) {
                delay = Execution.DelayInestimable;
            }
        }
    }

    /**
     * @version 2018/07/08 11:32:36
     */
    private static class Internals {

        private String id;
    }
}
