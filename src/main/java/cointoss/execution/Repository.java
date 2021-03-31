/*
 * Copyright (C) 2021 cointoss Development Team
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

import org.apache.commons.lang3.ObjectUtils;

import cointoss.MarketService;
import cointoss.util.Chrono;
import kiss.Signal;
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
    public LocalDate localScanLatest = LocalDate.of(1970, 1, 1);

    /** The oldest cache. */
    public LocalDate localFirst;

    /** The latest cache. */
    public LocalDate localLast;

    /** The last scan date-time in external repository. */
    public LocalDate externalScanLatest = LocalDate.of(1970, 1, 1);

    /** The oldest cache. */
    public LocalDate externalFirst;

    /** The latest cache. */
    public LocalDate externalLast;

    /**
     * Initialize.
     */
    Repository(Directory root, MarketService service) {
        this.root = root;
        this.service = service;

        restore();

        scanLocalRepository();
        scanExternalRepository();
    }

    /**
     * Scan logs in the local repository.
     */
    private void scanLocalRepository() {
        LocalDate now = LocalDate.now(Chrono.UTC);

        if (now.isAfter(localScanLatest)) {
            LocalDate[] dates = new LocalDate[2];

            root.walkFile("execution*.*og")
                    .map(file -> LocalDate.parse(file.name().subSequence(9, 17), Chrono.DateCompact))
                    .effectOnce(date -> {
                        dates[0] = date;
                        dates[1] = date;
                    })
                    .to(date -> {
                        if (date.isBefore(dates[0])) {
                            dates[0] = date;
                        } else if (date.isAfter(dates[1])) {
                            dates[1] = date;
                        }
                    }, e -> {
                        // ignore
                    }, () -> {
                        localFirst = dates[0];
                        localLast = dates[1];
                        localScanLatest = now;
                        store();
                    });
        }
    }

    /**
     * Scan logs in the external repository.
     */
    private void scanExternalRepository() {
        ExecutionLogRepository external = service.externalRepository();

        if (external == null) {
            return;
        }

        LocalDate now = LocalDate.now(Chrono.UTC);

        if (now.isAfter(externalScanLatest)) {
            LocalDate[] dates = new LocalDate[2];

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
     * Collect all managed date-times.
     * 
     * @return
     */
    Signal<ZonedDateTime> collectLocals() {
        return collectLocals(true, true);
    }

    /**
     * Collect all managed date-times.
     * 
     * @return
     */
    Signal<ZonedDateTime> collectLocals(boolean ascending) {
        return collectLocals(ascending, true);
    }

    /**
     * Collect all managed date-times.
     * 
     * @param ascending
     * @param includeToday
     * @return
     */
    Signal<ZonedDateTime> collectLocals(boolean ascending, boolean includeToday) {
        return new Signal<>((observer, disposer) -> {
            LocalDate last = !includeToday && localLast.isEqual(LocalDate.now(Chrono.UTC)) ? localLast.minusDays(1) : localLast;
            LocalDate current = ascending ? localFirst : last;

            while (!disposer.isDisposed()) {
                observer.accept(Chrono.utc(current));

                if (ascending) {
                    current = current.plusDays(1);
                    if (current.isAfter(last)) {
                        observer.complete();
                        break;
                    }
                } else {
                    current = current.plusDays(-1);
                    if (current.isBefore(localFirst)) {
                        observer.complete();
                        break;
                    }
                }
            }
            return disposer;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String locate() {
        return root.file("repository.json").toString();
    }
}