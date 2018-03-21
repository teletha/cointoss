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
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
        Thread thread = new Thread(run);
        thread.setName("Log Writer");
        thread.setDaemon(true);
        return thread;
    });

    /** file data format */
    private static final DateTimeFormatter fomatFile = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** realtime data format */
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.n'Z'");

    /** The log folder. */
    private final Path root;

    /** The current type. */
    private final BitFlyer type;

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
     * @param type
     */
    BitFlyerLog(BitFlyer type) {
        try {
            this.type = type;
            this.root = cacheRoot();

            List<Path> files = Filer.walk(root, "execution*.log");
            ZonedDateTime start = null;
            ZonedDateTime end = null;

            for (Path file : files) {
                String name = file.getFileName().toString();
                ZonedDateTime date = LocalDate.parse(name.substring(9, 17), fomatFile).atTime(0, 0, 0, 0).atZone(Chrono.UTC);

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
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path cacheRoot() {
        return Paths.get(".log/bitflyer/" + type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Signal<Execution> from(ZonedDateTime start) {
        return new Signal<Execution>((observer, disposer) -> {
            // read from cache
            if (cacheFirst != null) {
                ZonedDateTime current = start.isBefore(cacheFirst) ? cacheFirst : start.isAfter(cacheLast) ? cacheLast : start;
                current = current.withHour(0).withMinute(0).withSecond(0).withNano(0);

                while (disposer.isDisposed() == false && !current.isAfter(getCacheEnd())) {
                    disposer.add(localCache(current).effect(e -> latestId = cacheId = e.id)
                            .take(e -> e.exec_date.isAfter(start))
                            .to(observer::accept));
                    current = current.plusDays(1);
                }
            }

            AtomicBoolean completeREST = new AtomicBoolean();

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
    private void cache(Execution exe) {
        if (cacheId < exe.id) {
            cacheId = exe.id;

            writer.submit(() -> {
                try {
                    ZonedDateTime date = exe.exec_date;

                    if (cache == null || cacheLast.isBefore(date)) {
                        I.quiet(cache);

                        File file = localCacheFile(date).toFile();
                        file.createNewFile();

                        cache = new BufferedWriter(new FileWriter(file, true));
                        cacheLast = date.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
                    }
                    cache.write(exe.toString() + "\r\n");
                    cache.flush();
                } catch (IOException e) {
                    throw I.quiet(e);
                }
            });
        }
    }

    /**
     * Read date from local cache.
     * 
     * @param date
     * @return
     */
    private Signal<Execution> localCache(ZonedDateTime date) {
        return Filer.read(localCacheFile(date)).map(Execution::new);
    }

    /**
     * Read date from local cache.
     * 
     * @param date
     * @return
     */
    private Path localCacheFile(ZonedDateTime date) {
        return root.resolve("execution" + fomatFile.format(date) + ".log");
    }

    /**
     * Read data from REST API.
     */
    private Signal<Execution> rest() {
        return new Signal<Execution>((observer, disposer) -> {
            int offset = 1;

            while (disposer.isDisposed() == false) {
                try {
                    URL url = new URL(BitFlyerBackend.api + "/v1/executions?product_code=" + type + "&count=500&before=" + (latestId + 500 * offset));
                    Executions executions = I.json(url).to(Executions.class);

                    // skip if there is no new execution
                    if (executions.get(0).id == latestId) {
                        offset++;
                        continue;
                    }
                    offset = 1;

                    for (int i = executions.size() - 1; 0 <= i; i--) {
                        Execution exe = executions.get(i);

                        if (latestId < exe.id) {
                            observer.accept(exe);
                            latestId = exe.id;
                        }
                    }
                } catch (Exception e) {
                    observer.error(e);
                }

                if (realtimeId != 0 && realtimeId <= latestId) {
                    break;
                }

                try {
                    Thread.sleep(222);
                } catch (InterruptedException e) {
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
        return Network.pubnub("lightning_executions_" + type, "sub-c-52a9ab50-291b-11e5-baaa-0619f8945a4f")
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
}
