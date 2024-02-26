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
import java.util.function.Consumer;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.execution.ExecutionLog.Cache;
import cointoss.market.MarketServiceProvider;
import cointoss.util.Chrono;
import kiss.I;
import psychopath.Locator;

/**
 * Migration Tool.
 */
public class ExecutionLogTool {

    public static void main(String[] args) {
        I.load(Market.class);

        // deleteRepositoryInfo();
        changeLayout();
    }

    public static void changeLayout() {
        Locator.directory(".log").walkDirectory("*").to(dir -> {
            dir.walkDirectory("*").to(root -> {
                root.walkFile("execution*").to(file -> {
                    String name = file.name();
                    String year = name.substring(9, 13);
                    String month = name.substring(13, 15);

                    file.moveTo(root.directory("executions/" + year + "/" + month), o -> o.skipExisting());
                    System.out.println(dir.name() + root.name() + " " + name);
                });
            });
        });
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

    /**
     * Restore normal log of the specified market and date.
     * 
     * @param service
     * @param date
     */
    public static void restoreNormal(MarketService service, ZonedDateTime date) {
        ExecutionLog log = new ExecutionLog(service);
        Cache cache = log.cache(date);
        cache.convertCompactToNormal();
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
}