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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.JsonElement;

import cointoss.Execution;
import cointoss.MarketLog;
import cointoss.Side;
import cointoss.util.Chrono;
import cointoss.util.Network;
import cointoss.util.Num;
import filer.Filer;
import kiss.I;
import kiss.Signal;

/**
 * @version 2017/10/01 8:41:58
 */
class BitFlyerLog extends MarketLog {

    /** The writer thread. */
    private static final ExecutorService writer = Executors.newSingleThreadExecutor(run -> {
        final Thread thread = new Thread(run);
        thread.setName("Log Writer");
        thread.setDaemon(true);
        return thread;
    });

    /** file data format */
    private static final DateTimeFormatter fomatFile = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** realtime data format */
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.n'Z'");

    /** The latest execution id. */
    private long latestId = 23164000;

    /** The latest cached id. */
    private long cacheId;

    /** The latest realtime id. */
    private long realtimeId;

    /** The first day. */
    private final ZonedDateTime cacheFirst;

    /** The last day. */
    private ZonedDateTime cacheLast;

    /** The current processing cache file. */
    private BufferedWriter cache;

    /**
     * Bitflyer log manager.
     * 
     * @param provider
     */
    BitFlyerLog(BitFlyer provider) {
        super(provider);

        try {
            ZonedDateTime start = null;
            ZonedDateTime end = null;

            for (final Path file : Filer.walk(root, "execution*.log").toList()) {
                final String name = file.getFileName().toString();
                final ZonedDateTime date = LocalDate.parse(name.substring(9, 17), fomatFile).atTime(0, 0, 0, 0).atZone(Chrono.UTC);

                if (start == null || end == null) {
                    start = date;
                    end = date;
                } else {
                    if (start.isAfter(date)) {
                        start = date;
                    }

                    if (end.isBefore(date)) {
                        end = date;
                    }
                }
            }
            this.cacheFirst = start;
            this.cacheLast = end;
        } catch (final Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Signal<Execution> from(final ZonedDateTime start) {
        return new Signal<Execution>((observer, disposer) -> {
            // read from cache
            if (cacheFirst != null) {
                ZonedDateTime current = start.isBefore(cacheFirst) ? cacheFirst : start.isAfter(cacheLast) ? cacheLast : start;
                current = current.withHour(0).withMinute(0).withSecond(0).withNano(0);

                while (disposer.isDisposed() == false && !current.isAfter(getCacheEnd())) {
                    disposer.add(read(current).effect(e -> latestId = cacheId = e.id)
                            .take(e -> e.exec_date.isAfter(start))
                            .to(observer::accept));
                    current = current.plusDays(1);
                }
            }

            final AtomicBoolean completeREST = new AtomicBoolean();

            // read from realtime API
            if (disposer.isDisposed() == false) {
                disposer.add(realtime().skipUntil(e -> completeREST.get()).effect(this::cache).to(observer::accept));
            }

            // read from REST API
            if (disposer.isDisposed() == false) {
                disposer.add(rest().effect(this::cache).effectOnComplete(() -> completeREST.set(true)).to(observer::accept));
            }

            return disposer;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTime getCacheStart() {
        return cacheFirst;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTime getCacheEnd() {
        return cacheLast;
    }

    /**
     * Store cache data.
     * 
     * @param exe
     */
    private void cache(final Execution exe) {
        if (cacheId < exe.id) {
            cacheId = exe.id;

            writer.submit(() -> {
                try {
                    final ZonedDateTime date = exe.exec_date;

                    if (cache == null || cacheLast.isBefore(date)) {
                        I.quiet(cache);

                        final File file = localCacheFile(date).toFile();
                        file.createNewFile();

                        cache = new BufferedWriter(new FileWriter(file, true));
                        cacheLast = date.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
                    }
                    cache.write(exe.toString() + "\r\n");
                    cache.flush();
                } catch (final IOException e) {
                    throw I.quiet(e);
                }
            });
        }
    }

    /**
     * Read data from REST API.
     */
    private Signal<Execution> rest() {
        return new Signal<Execution>((observer, disposer) -> {
            int offset = 1;

            while (disposer.isDisposed() == false) {
                try {
                    final URL url = new URL(BitFlyerBackend.api + "/v1/executions?product_code=" + provider + "&count=500&before=" + (latestId + 500 * offset));
                    final Executions executions = I.json(url).to(Executions.class);

                    // skip if there is no new execution
                    if (executions.get(0).id == latestId) {
                        offset++;
                        continue;
                    }
                    offset = 1;

                    for (int i = executions.size() - 1; 0 <= i; i--) {
                        final Execution exe = executions.get(i);

                        if (latestId < exe.id) {
                            observer.accept(exe);
                            latestId = exe.id;
                        }
                    }
                } catch (final Exception e) {
                    // ignore to retry
                }

                if (realtimeId != 0 && realtimeId <= latestId) {
                    break;
                }

                try {
                    Thread.sleep(222);
                } catch (final InterruptedException e) {
                    observer.error(e);
                }
            }
            observer.complete();

            return disposer;
        });
    }

    /**
     * Read data from realtime API.
     * 
     * @return
     */
    private Signal<Execution> realtime() {
        return Network.pubnub("lightning_executions_" + provider, "sub-c-52a9ab50-291b-11e5-baaa-0619f8945a4f")
                .flatIterable(JsonElement::getAsJsonArray)
                .map(JsonElement::getAsJsonObject)
                .map(e -> {
                    Execution exe = new Execution();
                    exe.id = e.get("id").getAsLong();
                    exe.side = Side.parse(e.get("side").getAsString());
                    exe.price = Num.of(e.get("price").getAsString());
                    exe.size = exe.cumulativeSize = Num.of(e.get("size").getAsString());
                    exe.exec_date = LocalDateTime.parse(e.get("exec_date").getAsString(), format).atZone(BitFlyerBackend.zone);
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
        if (value == null) {
            return defaults;
        }
        return Long.parseLong(value);
    }

    private static Side decode(String value, Side defaults) {
        if (value == null) {
            return defaults;
        }
        return Side.parse(value);
    }

    private static Num decode(String value, Num defaults) {
        if (value == null) {
            return defaults;
        }
        return Num.of(value).plus(defaults);
    }

    private static Num decodeSize(String value, Num defaults) {
        if (value == null) {
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
        return encode(current.toInstant().toEpochMilli(), previous.toInstant().toEpochMilli(), 0);
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
