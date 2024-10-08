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

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.ObjectUtils;

import cointoss.MarketService;
import cointoss.util.Chrono;
import cointoss.util.Network;
import kiss.I;
import kiss.Managed;
import kiss.Storable;
import psychopath.Directory;

/**
 * Store for reepository related info.
 */
class Repository implements Storable<Repository> {

    /** The root directory of local repository. */
    private final Directory root;

    private final MarketService service;

    /** The last scan date-time in local repository. */
    @Managed
    private LocalDate localScanLatest = LocalDate.of(1970, 1, 1);

    /** The oldest cache. */
    @Managed
    private LocalDate localFirst;

    /** The latest cache. */
    @Managed
    private LocalDate localLast;

    /** The last scan date-time in external repository. */
    @Managed
    private LocalDate externalScanLatest = LocalDate.of(1970, 1, 1);

    /** The oldest cache. */
    @Managed
    private LocalDate externalFirst;

    /** The latest cache. */
    @Managed
    private LocalDate externalLast;

    /**
     * Initialize.
     */
    Repository(Directory root, MarketService service) {
        this.root = root;
        this.service = service;

        restore();

        Network.THREADS.submit(this::scanLocalRepository);
        Network.THREADS.submit(this::scanExternalRepository);
    }

    /**
     * Scan logs in the local repository.
     */
    private void scanLocalRepository() {
        LocalDate now = LocalDate.now(Chrono.UTC);
        if (now.isAfter(localScanLatest) || true) {
            root.walkDirectory("executions/*")
                    .first()
                    .flatMap(year -> year.walkDirectory("*"))
                    .first()
                    .flatMap(month -> month.walkFile("execution*.{log,clog}"))
                    .to(day -> {
                        localFirst = LocalDate.parse(day.base().subSequence(9, 17), Chrono.DateCompact);

                        // limit to today
                        if (localFirst.isAfter(now)) {
                            localFirst = now;
                        }
                    });

            root.walkDirectory("executions/*")
                    .last()
                    .flatMap(year -> year.walkDirectory("*"))
                    .last()
                    .flatMap(month -> month.walkFile("execution*.{log,clog}"))
                    .to(day -> {
                        localLast = LocalDate.parse(day.base().subSequence(9, 17), Chrono.DateCompact);

                        // limit to today
                        if (localLast.isAfter(now)) {
                            localLast = now;
                        }

                        // limit to first
                        if (localLast.isBefore(localFirst)) {
                            localLast = localFirst;
                        }
                    });

            localScanLatest = now;

            I.info("Scan log repository from " + localFirst + " \tto " + localLast + " [" + service.formattedId + "]");
            store();
        }
    }

    /**
     * Scan logs in the external repository.
     */
    private void scanExternalRepository() {
        LogHouse external = service.loghouse();

        if (!external.isValid()) {
            return;
        }

        LocalDate now = LocalDate.now(Chrono.UTC);
        LocalDate[] dates = new LocalDate[2];

        if (now.isAfter(externalScanLatest)) {
            external.collect().map(ZonedDateTime::toLocalDate).effectOnce(date -> {
                dates[0] = date;
                dates[1] = date;
            }).waitForTerminate().to(date -> {
                if (date.isBefore(dates[0])) {
                    dates[0] = date;
                } else if (date.isAfter(dates[1])) {
                    dates[1] = date;
                }
            }, e -> {
                // ignore
            }, () -> {
                externalFirst = dates[0];
                externalLast = dates[1];
                externalScanLatest = now;
                store();
            });
        }
    }

    /**
     * Compute the first cache.
     * 
     * @return
     */
    private LocalDate first() {
        return ObjectUtils.min(localFirst, externalFirst, LocalDate.now(Chrono.UTC));
    }

    /**
     * Compute the last cache.
     * 
     * @return
     */
    private LocalDate last() {
        return ObjectUtils.max(localLast, externalLast, first());
    }

    /**
     * Compute the first cache.
     * 
     * @return
     */
    ZonedDateTime firstZDT() {
        return first().atTime(0, 0).atZone(Chrono.UTC);
    }

    /**
     * Compute the last cache.
     * 
     * @return
     */
    ZonedDateTime lastZDT() {
        return last().atTime(0, 0).atZone(Chrono.UTC);
    }

    /**
     * Update the local resource.
     * 
     * @param date
     */
    void updateLocal(ZonedDateTime date) {
        updateLocal(date.toLocalDate());
    }

    /**
     * Update the local resource.
     * 
     * @param date
     */
    void updateLocal(LocalDate date) {
        if (localFirst == null || date.isBefore(localFirst)) {
            localFirst = date;
            store();
        } else if (localLast == null || date.isAfter(localLast)) {
            localLast = date;
            store();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path locate() {
        return root.file("repository.json").asJavaPath();
    }
}