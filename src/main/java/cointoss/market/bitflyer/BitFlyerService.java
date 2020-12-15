/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitflyer;

import static java.util.concurrent.TimeUnit.*;
import static kiss.I.*;
import static viewtify.ui.UIWeb.Operation.*;

import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.RandomStringUtils;

import com.google.common.hash.Hashing;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.market.Exchange;
import cointoss.order.Order;
import cointoss.order.OrderBookPage;
import cointoss.order.OrderBookPageChanges;
import cointoss.order.OrderManager;
import cointoss.order.OrderState;
import cointoss.order.OrderType;
import cointoss.util.APILimiter;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.JSON;
import kiss.Signal;
import viewtify.Viewtify;

/**
 * Since the order API and the real-time execution API are completely separate systems, execution
 * data may arrive before the order response, or execution data may arrive after the cancellation
 * response.
 */
public class BitFlyerService extends MarketService {

    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-");

    /** The bitflyer ID date fromat. */
    private static final DateTimeFormatter IdFormat = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    /** The API limit. */
    private static final APILimiter LIMITER = APILimiter.with.limit(500).refresh(5, MINUTES);

    /** The shared realtime communicator. It will be shared across all markets on this exchange. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://ws.lightstream.bitflyer.com/json-rpc")
            .extractId(json -> json.find(String.class, "params", "channel").toString())
            .whenConnected(ws -> ws.sendText(I.write(new Auth()), true));

    /** The realtime data format */
    static final DateTimeFormatter RealtimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /** The realtime data format */
    static final DateTimeFormatter RealtimeFormatUntilSecond = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /** The realtime data format */
    static final DateTimeFormatter RealtimeFormatUntilMinute = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    /** The realtime data format */
    static final DateTimeFormatter RealtimeFormatUntilHour = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH");

    /** Flag for test. */
    private final boolean forTest;

    /** The account setting. */
    private static final BitFlyerAccount account = I.make(BitFlyerAccount.class);

    /** The shared order list. */
    private final Signal<List<Order>> intervalOrderCheck;

    /** The session key. */
    private static final String sessionKey = "api_session_v2";

    /** The latest realtime execution id. */
    private long latestId;

    /**
     * @param type
     */
    protected BitFlyerService(String type, MarketSetting setting) {
        this(type, setting, false);
    }

    /**
     * Market service for bitFlyer.
     * 
     * @param marketId A market identifier.
     * @param setting A market setting.
     * @param forTest
     */
    protected BitFlyerService(String marketId, MarketSetting setting, boolean forTest) {
        super(Exchange.BitFlyer, marketId, setting);

        this.forTest = forTest;
        this.intervalOrderCheck = I.schedule(0, 1, TimeUnit.SECONDS, false, scheduler()).map(v -> orders().toList()).share();
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
        Signal<String> call;
        String id = "JRF" + Chrono.utcNow().format(format) + RandomStringUtils.randomNumeric(6);

        if (forTest || Session.id == null) {
            ChildOrderRequest request = new ChildOrderRequest();
            request.child_order_type = order.type == OrderType.Maker ? "LIMIT" : "MARKET";
            request.minute_to_expire = 60 * 24;
            request.price = order.price.intValue();
            request.product_code = marketName;
            request.side = order.direction().name();
            request.size = order.size.doubleValue();
            request.time_in_force = order.quantityCondition.abbreviation;

            call = rest("POST", API.Private, "/v1/me/sendchildorder", I.write(request))
                    .flatIterable(json -> json.find(String.class, "child_order_acceptance_id"));
        } else {
            ChildOrderRequestWebAPI request = new ChildOrderRequestWebAPI();
            request.account_id = account.accountId.v;
            request.ord_type = order.type == OrderType.Maker ? "LIMIT" : "MARKET";
            request.minute_to_expire = 60 * 24;
            request.order_ref_id = id;
            request.price = order.price.intValue();
            request.product_code = marketName;
            request.side = order.direction().name();
            request.size = order.size.doubleValue();
            request.time_in_force = order.quantityCondition.abbreviation;

            call = rest("POST", API.Internal, "/trade/sendorder", I.write(request)).map(json -> json.get("data").text("order_ref_id"));
        }

        return call;
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
        cancel.child_order_acceptance_id = order.id;

        Signal<?> call = forTest || Session.id == null || cancel.order_id == null
                ? rest("POST", API.Private, "/v1/me/cancelchildorder", I.write(cancel))
                : rest("POST", API.Internal, "/trade/cancelorder", I.write(cancel));

        Signal<Order> isCancelled = intervalOrderCheck.map(orders -> {
            for (Order listed : orders) {
                if (order.id.equals(listed.id)) {
                    if (listed.isTerminated()) {
                        return listed;
                    } else {
                        return null;
                    }
                }
            }

            return Order.with.direction(order.direction, order.size)
                    .id(order.id)
                    .state(OrderState.CANCELED)
                    .executedSize(order.executedSize);
        }).skipNull();

        return call.combine(isCancelled).take(1).map(v -> v.ⅱ);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        String[] previous = new String[] {"", ""};

        return clientRealtimely().subscribe(new Topic("lightning_executions_", marketName))
                .flatIterable(json -> json.find("params", "message", "*"))
                .map(json -> convertExecution(json, previous))
                .skipNull();
    }

    /**
     * Normalize time format.
     * 
     * @param time
     * @return
     */
    static LocalDateTime parse(String time) {
        int size = time.length();

        if (time.charAt(size - 1) != 'Z') {
            return LocalDateTime.parse(time);
        }

        // remove tail Z
        time = time.substring(0, size - 1);

        switch (size) {
        case 14:
            return LocalDateTime.parse(time, RealtimeFormatUntilHour);

        case 17:
            return LocalDateTime.parse(time, RealtimeFormatUntilMinute);

        case 20:
            return LocalDateTime.parse(time, RealtimeFormatUntilSecond);

        default:
            // padding 0
            if (size < 24) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < 24 - size; i++) {
                    builder.append("0");
                }
                time = time + builder;
            }
            return LocalDateTime.parse(time.substring(0, 23), RealtimeFormat);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long startId, long endId) {
        String[] previous = new String[] {"", ""};

        return rest("GET", API.Public, "/v1/executions?product_code=" + marketName + "&count=" + setting
                .acquirableExecutionSize() + "&after=" + startId + "&before=" + endId) //
                        .flatIterable(e -> e.find("*"))
                        .reverse()
                        .map(e -> convertExecution(e, previous));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        String[] previous = new String[] {"", ""};

        return rest("GET", API.Public, "/v1/executions?product_code=" + marketName + "&count=1").flatIterable(e -> e.find("*"))
                .map(e -> convertExecution(e, previous));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsBefore(long id) {
        String[] previous = new String[] {"", ""};

        return rest("GET", API.Public, "/v1/executions?product_code=" + marketName + "&count=1").flatIterable(e -> e.find("*"))
                .map(e -> convertExecution(e, previous));
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param json Message represents executed trade.
     * @param previous Previous execution info.
     * @return A parsed {@link Execution}.
     */
    private Execution convertExecution(JSON json, String[] previous) {
        long id = Long.parseLong(json.text("id"));
        if (id == 0 && latestId == 0) {
            return null; // skip
        }

        id = latestId = id != 0 ? id : ++latestId;
        Direction direction = json.get(Direction.class, "side");
        Num size = json.get(Num.class, "size");
        Num price = json.get(Num.class, "price");
        ZonedDateTime date = parse(json.text("exec_date")).atZone(Chrono.UTC);
        String buyer = json.text("buy_child_order_acceptance_id");
        String seller = json.text("sell_child_order_acceptance_id");
        String taker = direction.isBuy() ? buyer : seller;
        int consecutiveType = estimateConsecutiveType(previous[0], previous[1], buyer, seller, direction);
        int delay = estimateDelay(taker, date);

        previous[0] = buyer;
        previous[1] = seller;

        return Execution.with.direction(direction, size).id(id).price(price).date(date).consecutive(consecutiveType).delay(delay);
    }

    /**
     * Estimate consecutive type.
     * 
     * @param previous
     */
    private int estimateConsecutiveType(String prevBuyer, String prevSeller, String buyer, String seller, Direction side) {
        if (buyer.equals(prevBuyer)) {
            if (seller.equals(prevSeller)) {
                return side == Direction.BUY ? Execution.ConsecutiveSameBuyer : Execution.ConsecutiveSameSeller;
            } else {
                return Execution.ConsecutiveSameBuyer;
            }
        } else if (seller.equals(prevSeller)) {
            return Execution.ConsecutiveSameSeller;
        } else {
            return Execution.ConsecutiveDifference;
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
    private int estimateDelay(String taker, ZonedDateTime date) {
        try {
            // order format is like the following [JRF20180427-123407-869661]
            // exclude illegal format
            if (taker == null || taker.length() != 25 || !taker.startsWith("JRF")) {
                return Execution.DelayInestimable;
            }

            // remove tail random numbers
            taker = taker.substring(3, 18);

            // parse as datetime
            long orderTime = LocalDateTime.parse(taker, IdFormat).toEpochSecond(ZoneOffset.UTC);
            long executedTime = date.toEpochSecond() + 1;
            int diff = (int) (executedTime - orderTime);

            // estimate server order (over 9*60*60)
            if (diff < -32000) {
                diff += 32400;
            }

            if (diff < 0) {
                return Execution.DelayInestimable;
            } else if (180 < diff) {
                return Execution.DelayHuge;
            } else {
                return diff;
            }
        } catch (DateTimeParseException e) {
            return Execution.DelayInestimable;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> orders() {
        return rest("GET", API.Private, "/v1/me/getchildorders?product_code=" + marketName)
                .flatIterable(json -> json.find(ChildOrderResponse.class, "*"))
                .map(ChildOrderResponse::toOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> orders(OrderState state) {
        return rest("GET", API.Private, "/v1/me/getchildorders?child_order_state=" + state + "&product_code=" + marketName)
                .flatIterable(json -> json.find(ChildOrderResponse.class, "*"))
                .map(ChildOrderResponse::toOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Order> connectOrdersRealtimely() {
        // return orderUpdateRealtimely.expose;
        return clientRealtimely().subscribe(new Topic("child_order_events", ""))
                .flatIterable(json -> json.find("params", "message", "*"))
                .take(json -> json.has("product_code", marketName))
                .map(json -> {
                    System.out.println(json);

                    String type = json.text("event_type");
                    Direction side = json.get(Direction.class, "side");
                    Num price = json.get(Num.class, "price");
                    Num size = json.get(Num.class, "size");
                    String id = json.text("child_order_acceptance_id");

                    if (type.equals("ORDER")) {
                        return OrderManager.Update.create(id, side, size, price);
                    } else if (type.equals("CANCEL")) {
                        return OrderManager.Update.cancel(id);
                    } else if (type.equals("EXECUTION")) {
                        Num commission = price.multiply(json.get(Num.class, "commission")).scale(setting.base.scale, RoundingMode.CEILING);
                        Num sfd = json.get(Num.class, "sfd");
                        return OrderManager.Update.executePartially(id, size, price, commission.plus(sfd));
                    } else { /* ORDER_FAILED || CANCEL_FAILED || EXPIRE */
                        return (Order) null;
                    }
                })
                .skipNull();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> baseCurrency() {
        return rest("GET", API.Private, "/v1/me/getbalance").flatIterable(json -> json.find(CurrencyState.class, "*"))
                .take(unit -> unit.currency_code.equals("JPY"))
                .map(unit -> unit.available);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> targetCurrency() {
        return rest("GET", API.Private, "/v1/me/getbalance").flatIterable(json -> json.find(CurrencyState.class, "*"))
                .take(unit -> unit.currency_code.equals("BTC"))
                .map(unit -> unit.available);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookPageChanges> orderBook() {
        return rest("GET", API.Public, "/v1/board?product_code=" + marketName).map(json -> json.as(OrderBookPageChanges.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> createOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("lightning_board_", marketName)).map(root -> {
            JSON e = root.get("params").get("message");

            OrderBookPageChanges change = new OrderBookPageChanges();
            for (JSON ask : e.find("asks", "*")) {
                change.asks.add(new OrderBookPage(ask.get(Num.class, "price"), ask.get(Double.class, "size")));
            }

            for (JSON bid : e.find("bids", "*")) {
                change.bids.add(new OrderBookPage(bid.get(Num.class, "price"), bid.get(Double.class, "size")));
            }
            return change;
        });
    }

    /**
     * Execute REST operation.
     * 
     * @param method
     * @param api
     * @param uri
     * @param bodyContents
     * @return
     */
    private Signal<JSON> rest(String method, API api, String uri, String... bodyContents) {
        int weight = 1;
        URI u = URI.create(api + uri);
        String bodyText = String.join("", bodyContents);

        HttpRequest.Builder builder = HttpRequest.newBuilder(u);

        // authentication if needed
        switch (api) {
        case Public:
            break;

        case Private:
            weight = 2;
            String path = u.getPath();
            String query = u.getQuery();
            if (query != null) path = path + "?" + query;
            String timestamp = String.valueOf(Chrono.utcNow().toEpochSecond());
            String sign = Hashing.hmacSha256(account.apiSecret.v.getBytes())
                    .hashString(timestamp + method + path + bodyText, StandardCharsets.UTF_8)
                    .toString();

            builder = builder.header("ACCESS-KEY", account.apiKey.v)
                    .header("ACCESS-TIMESTAMP", timestamp)
                    .header("ACCESS-SIGN", sign)
                    .header("Content-Type", "application/json");
            break;

        case Internal:
            builder = builder.header("Content-Type", "application/json; charset=utf-8")
                    .header("Cookie", sessionKey + "=" + Session.id)
                    .header("X-Requested-With", "XMLHttpRequest");
            break;
        }

        // post with body contents
        if (method.equals("POST")) {
            builder = builder.POST(BodyPublishers.ofString(bodyText));
        }

        return Network.rest(builder, LIMITER, weight, client());
    }

    /**
     * Singleton session holder. The JVM specification guarantees automatic lazy initialization on
     * first access.
     */
    private static class Session {

        /** The session ID (may be null). */
        private static String id;

        // Use your browser to log in and obtain a session ID.
        static {
            Viewtify.browser(browser -> {
                browser.load("https://lightning.bitflyer.jp")
                        .$(input("#LoginId", account.loginId.exact()))
                        .$(input("#Password", account.loginPassword.exact()))
                        .$(click("#login_btn"))
                        .$(awaitContentLoading())
                        .$(detour("https://lightning.bitflyer.jp/Home/TwoFactorAuth", w -> {
                            return browser.click("form > label")
                                    .$(inputByHuman("#ConfirmationCode", translate("Enter the two-step verification code provided by Bitflyer.")))
                                    .$(click("form > button"))
                                    .$(awaitContentLoading());
                        }))
                        .to(() -> {
                            browser.cookie(sessionKey).to(c -> id = c.getValue());
                            browser.stage().get().close();

                            maintain();
                        });
            });
        }

        /**
         * Use the API periodically to maintain the session ID.
         */
        private static void maintain() {
            I.schedule(15, 15, TimeUnit.MINUTES, true).to(() -> {
                Builder builder = HttpRequest.newBuilder(URI.create("https://lightning.bitflyer.com/api/trade/getMyBoardOrders"))
                        .header("Content-Type", "application/json")
                        .header("Cookie", sessionKey + "=" + Session.id)
                        .header("X-Requested-With", "XMLHttpRequest")
                        .POST(BodyPublishers
                                .ofString("{\"product_code\":\"FX_BTC_JPY\",\"account_id\":\"" + account.accountId + "\",\"lang\":\"ja\"}"));

                Network.rest(builder, LIMITER, 1).to(I.NoOP);
            });
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

        public Num executed_size;

        public Num canceled_size;

        public String child_order_date;

        public OrderState child_order_state;

        public Order toOrder() {
            Order o = Order.with.direction(side, size)
                    .price(price)
                    .executedSize(executed_size)
                    .state(child_order_state)
                    .id(child_order_acceptance_id)
                    .type(OrderType.Maker)
                    .creationTime(LocalDateTime.parse(child_order_date, Chrono.DateTimeWithT).atZone(Chrono.UTC));
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
     * 
     */
    @SuppressWarnings("unused")
    private static class CircuitBreakerInfo {
        public Num upper_limit;

        public Num lower_limit;

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "CircuitBreakerInfo [upper_limit=" + upper_limit + ", lower_limit=" + lower_limit + "]";
        }
    }

    /**
     * @version 2018/07/08 11:32:36
     */
    private static class Internals {

        private String id;
    }

    /**
     * Topic on Realtime API.
     */
    static class Topic extends IdentifiableTopic<Topic> {

        private static final AtomicInteger counter = new AtomicInteger();

        public int id = counter.incrementAndGet();

        /** The string expression to make equality checking fast. */
        private String idText = Integer.toString(id);

        public String jsonrpc = "2.0";

        public String method = "subscribe";

        public Map<String, String> params = new HashMap();

        /**
         * @param id
         */
        public Topic(String channel, String marketName) {
            super("[" + channel + marketName + "]", topic -> topic.method = "unsubscribe");
            params.put("channel", channel + marketName);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean verifySubscribedReply(JSON reply) {
            return reply.has("id", idText) && Boolean.parseBoolean(reply.text("result"));
        }
    }

    static class Auth {

        public String method = "auth";

        public String jsonrpc = "2.0";

        public Params params = new Params();

        private Auth() {
            long timestamp = Chrono.utcNow().toEpochSecond();
            String nonce = RandomStringUtils.randomNumeric(16);
            String sign = Hashing.hmacSha256(account.apiSecret.v.getBytes())
                    .hashString(timestamp + nonce, StandardCharsets.UTF_8)
                    .toString();

            params.api_key = account.apiKey.v;
            params.timestamp = timestamp;
            params.nonce = nonce;
            params.signature = sign;
        }

        @SuppressWarnings("unused")
        private static class Params {

            public String api_key;

            public String signature;

            public String nonce;

            public long timestamp;
        }
    }

    /**
     * API Category
     */
    private enum API {
        Public("https://api.bitflyer.com"), Private("https://api.bitflyer.com"), Internal("https://lightning.bitflyer.jp/api");

        private final String uri;

        /**
         * @param uri
         */
        private API(String uri) {
            this.uri = uri;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return uri;
        }
    }
}