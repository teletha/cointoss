/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.execution;

import java.awt.Desktop;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.function.Consumer;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.execution.ExecutionLog.Cache;
import cointoss.market.MarketServiceProvider;
import cointoss.market.TimestampBasedMarketServiceSupporter;
import cointoss.market.gmo.GMO;
import cointoss.util.Chrono;
import kiss.I;
import kiss.Signal;
import psychopath.File;

/**
 * Migration Tool.
 */
public class ExecutionLogTool {

    public static void main(String[] args) {
        I.load(Market.class);

        restoreNormal(GMO.BTC, Chrono.utc(2024, 9, 17));
    }

    /**
     * Restore normal log of the specified market and date.
     * 
     * @param service
     * @param date
     */
    public static void repairLog(MarketService service, ZonedDateTime date) {
        ExecutionLog log = new ExecutionLog(service);
        Cache cache = log.cache(date);
        cache.repair(false);
    }

    public static void convertToTimestampBasedId(MarketService idBased, MarketService timeBased) {
        long[] context = new long[3];
        TimestampBasedMarketServiceSupporter support = new TimestampBasedMarketServiceSupporter();

        idBased.log.caches().to(cache -> {
            LocalDate date = cache.date;
            if (!timeBased.log.cache(date).existCompact()) {
                Signal<Execution> exe = idBased.log.cache(date)
                        .read(LogType.Normal)
                        .map(x -> support.createExecution(x.orientation, x.size, x.price, x.date, context));

                Cache destCache = timeBased.log.cache(date);
                destCache.writeFast(destCache.writeCompact(exe)).to(I.NoOP);
                System.out.println("Convert to " + timeBased + " " + date);
            }
        });
    }

    /**
     * Create the fast log from normal log.
     */
    public static void createFastLog(MarketService service) {
        ExecutionLog log = new ExecutionLog(service);
        log.caches().effect(e -> System.out.println(e)).to(Cache::buildFast);
    }

    /**
     * Create the fast log from normal log.
     */
    public static void createFastLog(MarketService service, ZonedDateTime date) {
        ExecutionLog log = new ExecutionLog(service);
        Cache cache = log.cache(date);
        cache.buildFast();
    }

    /**
     * Restore normal log of the specified market and date.
     * 
     * @param service
     * @param date
     */
    public static Operated restoreNormal(MarketService service, ZonedDateTime date) {
        ExecutionLog log = new ExecutionLog(service);
        Cache cache = log.cache(date);
        cache.convertCompactToNormal();

        return new Operated(cache.normal);
    }

    /**
     * Delete all fast logs.
     */
    public static void deleteRepositoryInfo() {
        processLog(log -> {
            log.clearRepositoryInfo();
        });
    }

    /**
     * Delete all fast logs.
     */
    public static void deleteFastLog() {
        processLog(log -> {
            log.clearFastCache();
        });
    }

    /**
     * Reads the last ID from latest compact logs and sets it as the creation date of the file.
     */
    public static void setLastIdOnCompactLog() {
        LocalDate start = Chrono.utcToday().minusDays(15).toLocalDate();

        processLog(log -> {
            log.caches().take(c -> c.date.isAfter(start)).to(c -> {
                c.readCompact().last().to(e -> {
                    c.compactLog().creationTime(e.id);
                    System.out.println("Set last ID as compact log's creation time on " + c);
                });
            });
        });
    }

    /**
     * Template.
     */
    private static void processLog(Consumer<ExecutionLog> service) {
        I.load(Market.class);
        MarketServiceProvider.availableMarketServices().map(ExecutionLog::new).to(service);
    }

    private static class Operated {
        private File file;

        Operated(File file) {
            this.file = file;
        }

        private void open() {
            try {
                Desktop.getDesktop().open(file.asJavaFile());
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }
}