/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.apache.commons.codec.digest.HmacUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import cointoss.BalanceUnit;
import cointoss.Execution;
import cointoss.Market;
import cointoss.MarketService;
import cointoss.Order;
import cointoss.OrderState;
import cointoss.Position;
import filer.Filer;
import kiss.I;
import kiss.Signal;

/**
 * @version 2017/07/22 13:18:23
 */
class BitFlyerService implements MarketService {

    /** The api url. */
    static final String api = "https://api.bitflyer.jp";

    /** UTC */
    static final ZoneId zone = ZoneId.of("UTC");

    /** The market type. */
    private final BitFlyer type;

    /** The key. */
    private final String accessKey;

    /** The token. */
    private final String accessToken;

    /**
     * @param type
     */
    BitFlyerService(BitFlyer type) {
        List<String> lines = Filer.read(".log/bitflyer/key.txt").toList();

        this.type = type;
        this.accessKey = lines.get(0);
        this.accessToken = lines.get(1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(Market market, Signal<Execution> log) {
        log.to(market::tick);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<String> request(Order order) {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<String> cancel(String childOrderId) {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> getOrderBy(String id) {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> getOrders() {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> getOrdersBy(OrderState state) {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Position> getPositions() {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> getExecutions() {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<BalanceUnit> getCurrency() {
        return call("GET", "/v1/me/getbalance", "", "*", BalanceUnit.class)
                .take(unit -> unit.currency_code.equals("JPY") || unit.currency_code.equals("BTC"));
    }

    /**
     * Call private API.
     */
    private <M> Signal<M> call(String method, String path, String body, String selector, Class<M> type) {
        return new Signal<>((observer, disposer) -> {
            String timestamp = String.valueOf(ZonedDateTime.now(zone).toEpochSecond());
            String sign = HmacUtils.hmacSha256Hex(accessToken, timestamp + method + path + body);

            HttpGet request = new HttpGet(api + path);
            request.addHeader("ACCESS-KEY", accessKey);
            request.addHeader("ACCESS-TIMESTAMP", timestamp);
            request.addHeader("ACCESS-SIGN", sign);

            try (CloseableHttpClient client = HttpClientBuilder.create().disableCookieManagement().build(); //
                    CloseableHttpResponse response = client.execute(request)) {

                int status = response.getStatusLine().getStatusCode();

                if (status == HttpStatus.SC_OK) {
                    I.json(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8)).find(selector, type).to(observer::accept);
                } else {
                    observer.error(new Error("HTTP Status " + status));
                }
            } catch (Exception e) {
                observer.error(e);
            }
            return disposer;
        });
    }
}
