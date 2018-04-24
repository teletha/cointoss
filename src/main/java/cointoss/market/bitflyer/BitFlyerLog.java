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

import java.io.BufferedWriter;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.JsonElement;

import cointoss.Execution;
import cointoss.MarketLog;
import cointoss.Side;
import cointoss.util.Network;
import cointoss.util.Num;
import kiss.I;
import kiss.Signal;

/**
 * @version 2017/10/01 8:41:58
 */
class BitFlyerLog extends MarketLog {

    /** realtime data format */
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * Bitflyer log manager.
     * 
     * @param provider
     */
    BitFlyerLog(BitFlyer provider) {
        super(provider);
    }

    /**
     * Read data from realtime API.
     * 
     * @return
     */
    @Override
    public Signal<Execution> realtime() {
        return Network.pubnub("lightning_executions_" + provider, "sub-c-52a9ab50-291b-11e5-baaa-0619f8945a4f")
                .flatIterable(JsonElement::getAsJsonArray)
                .map(JsonElement::getAsJsonObject)
                .map(e -> {
                    Execution exe = new Execution();
                    exe.id = e.get("id").getAsLong();
                    exe.side = Side.parse(e.get("side").getAsString());
                    exe.price = Num.of(e.get("price").getAsString());
                    exe.size = exe.cumulativeSize = Num.of(e.get("size").getAsString());
                    exe.exec_date = LocalDateTime.parse(e.get("exec_date").getAsString().substring(0, 23), format)
                            .atZone(BitFlyerBackend.zone);
                    exe.buy_child_order_acceptance_id = e.get("buy_child_order_acceptance_id").getAsString();
                    exe.sell_child_order_acceptance_id = e.get("sell_child_order_acceptance_id").getAsString();

                    if (exe.id == 0) {
                        exe.id = ++realtimeId;
                    }
                    realtimeId = exe.id;
                    return exe;
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
            String buyer = encode(execution.buyer(), previous.buyer(), 0);
            String seller = encode(execution.seller(), previous.seller(), 0);

            return new String[] {id, time, side, price, size, buyer, seller};
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
}
