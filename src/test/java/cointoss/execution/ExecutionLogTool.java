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

import java.time.LocalDate;
import java.time.ZonedDateTime;

import cointoss.MarketService;
import cointoss.execution.ExecutionLog.Cache;
import cointoss.market.TimestampBasedMarketServiceSupporter;
import cointoss.market.bitflyer.BitFlyer;
import kiss.I;
import kiss.Signal;

/**
 * Migration Tool.
 */
public class ExecutionLogTool {

    public static void main(String[] args) {
        Tool.defineTask().on(BitFlyer.BTC_JPY).at(2024, 9, 17).run(ExecutionLogTool::createFastLog);
    }

    public static void convertToTimestampBasedId(ExecutionLog idBased, MarketService timeBased) {
        long[] context = new long[3];
        TimestampBasedMarketServiceSupporter support = new TimestampBasedMarketServiceSupporter();

        idBased.caches().to(cache -> {
            LocalDate date = cache.date;
            if (!timeBased.log.cache(date).existCompact()) {
                Signal<Execution> exe = idBased.cache(date)
                        .read(LogType.Normal)
                        .map(x -> support.createExecution(x.orientation, x.size, x.price, x.date, context));

                Cache destCache = timeBased.log.cache(date);
                destCache.writeFast(destCache.writeCompact(exe)).to(I.NoOP);
                System.out.println("Convert to " + timeBased + " " + date);
            }
        });
    }

    /**
     * Restore normal log of the specified market and date.
     */
    public static void repairLog(ExecutionLog log, ZonedDateTime date) {
        Cache cache = log.cache(date);
        cache.repair(false);
    }

    /**
     * Create the fast log from normal log.
     */
    public static void createFastLog(ExecutionLog log, ZonedDateTime date) {
        Cache cache = log.cache(date);
        cache.buildFast();
    }

    /**
     * Restore normal log of the specified market and date.
     */
    public static void restoreNormal(ExecutionLog log, ZonedDateTime date) {
        Cache cache = log.cache(date);
        cache.convertCompactToNormal();
    }

    /**
     * Change the compression level of compact log.
     * 
     * @param level
     */
    public static void convertCompactLevel(ExecutionLog log, int level) {
        long[] total = {0, 0};

        log.caches().to(cache -> {
            if (cache.existCompact()) {
                long old = cache.compactLog().size();
                cache.convertCompactLevel(level);
                long renew = cache.compactLog().size();

                total[0] += old;
                total[1] += renew;

                show(log.service + " convert the compression level at " + cache.date + ".", old, renew);
            }
        });
        show(log.service + " compress log.", total[0], total[1]);
    }

    /**
     * Show message with compression ratio.
     * 
     * @param message
     * @param old
     * @param renew
     */
    private static void show(String message, long old, long renew) {
        long ratio = Math.round(renew * 100 / old);
        System.out.println(message + " (" + formatSize(old) + " -> " + formatSize(renew) + "  " + ratio + "%)");
    }

    /**
     * Format the file size.
     * 
     * @param bytes
     * @return
     */
    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char prefix = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f%sB", bytes / Math.pow(1024, exp), prefix);
    }

    /**
     * Delete all fast logs.
     */
    public static void deleteRepositoryInfo(ExecutionLog log) {
        log.clearRepositoryInfo();
    }

    /**
     * Delete fast log.
     */
    public static void deleteFastLog(ExecutionLog log, ZonedDateTime date) {
        log.cache(date).fastLog().delete();
    }

    /**
     * Delete compact log.
     */
    public static void deleteCompactLog(ExecutionLog log, ZonedDateTime date) {
        log.cache(date).compactLog().delete();
    }
}