/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import static java.time.temporal.ChronoUnit.*;

import java.time.ZonedDateTime;
import java.util.function.Function;

import cointoss.Direction;
import cointoss.Market;
import cointoss.MarketService;
import cointoss.execution.Execution;
import cointoss.execution.LogHouse;
import cointoss.execution.LogType;
import cointoss.util.Chrono;
import cointoss.util.DateRange;
import cointoss.util.feather.FeatherStore;
import hypatia.Num;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import kiss.Variable;

public class TickerManager implements Disposable {

    /** The latest execution. */
    public final Variable<Execution> latest = Variable.of(Market.BASE);

    /** The associated service. */
    public final MarketService service;

    /** Total of long volume since application startup. */
    double longVolume = 0;

    /** Total of long losscut volume since application startup. */
    double longLosscutVolume = 0;

    /** Total of short volume since application startup. */
    double shortVolume = 0;

    /** Total of short losscut volume since application startup. */
    double shortLosscutVolume = 0;

    /** The number of tickers. */
    private final int size = Span.values().length;

    /** The managed tickers. */
    private final Ticker[] tickers = new Ticker[size];

    /** The initialization state. */
    private boolean initialized;

    public TickerManager() {
        this((MarketService) null);
    }

    /**
     * Create {@link TickerManager}.
     */
    public TickerManager(MarketService service) {
        this(service, null);
    }

    /**
     * Create {@link TickerManager}.
     */
    public TickerManager(MarketService service, Function<Span, FeatherStore<Tick>> source) {
        this.service = service;

        for (int i = size - 1; 0 <= i; i--) {
            Ticker ticker = tickers[i] = new Ticker(Span.values()[i], this, source);

            // cache associated upper tickers
            int index = 0;
            for (int upper : ticker.span.uppers) {
                ticker.uppers[index++] = this.tickers[upper];
            }
        }
    }

    /**
     * Retrieve the {@link Ticker} by {@link Span}.
     * 
     * @param span The target {@link Span}.
     */
    public Ticker on(Span span) {
        return tickers[span.ordinal()];
    }

    /**
     * Retrieve all {@link Ticker}s.
     * 
     * @return
     */
    public Signal<Ticker> tickers() {
        return I.signal(tickers);
    }

    /**
     * Update all {@link Ticker}s by {@link Execution}.
     * 
     * @param e The latest {@link Execution}.
     */
    public void update(Execution e) {
        if (initialized == false) {
            // initialize tickers once if needed
            initialized = true;
            // for the first time only, set LTP before updating tickers
            latest.set(e);

            for (Ticker ticker : tickers) {
                ticker.init(e);
            }
        } else {
            // update tickers
            update(tickers[0], e, e.price, e.price.compareTo(latest.v.price));
        }

        // update total related values
        if (e.orientation == Direction.BUY) {
            longVolume += e.size.doubleValue();
            if (e.delay == Execution.DelayHuge) {
                shortLosscutVolume += e.size.doubleValue();
            }
        } else {
            shortVolume += e.size.doubleValue();
            if (e.delay == Execution.DelayHuge) {
                longLosscutVolume += e.size.doubleValue();
            }
        }

        // update the latest execution at last
        latest.set(e);
    }

    /**
     * Update the specified {@link Ticker}.
     * 
     * @param ticker A target ticker to update.
     * @param execution A latest {@link Execution}.
     * @param price A latest price to cache.
     * @param comparisonResult The comparison result between previous price and current price.
     */
    private void update(Ticker ticker, Execution execution, Num price, int comparisonResult) {
        if (ticker.createTick(execution)) {
            for (Ticker upper : ticker.uppers) {
                update(upper, execution, price, comparisonResult);
            }
        } else {
            // If a new tick is not added, the maximum value and the minimum value will be updated.
            switch (comparisonResult) {
            case 1:
                // If it is higher than the previous price, since it is impossible to update the
                // minimum price in all upper tickers, only update the maximum price.
                updateHighPrice(ticker, price);
                break;
            case -1:
                // If it is lower than the previous price, since it is impossible to update the
                // maximum price in all upper tickers, only update the minimum price.
                updateLowPrice(ticker, price);
                break;
            }
        }
    }

    /**
     * Update high price of the specified {@link Ticker}.
     * 
     * @param ticker A target {@link Ticker} to update high price.
     * @param price A current price.
     */
    private void updateHighPrice(Ticker ticker, Num price) {
        if (price.isGreaterThan(ticker.current.highPrice)) {
            ticker.current.highPrice = price.doubleValue();

            for (Ticker upper : ticker.uppers) {
                updateHighPrice(upper, price);
            }
        }
    }

    /**
     * Update low price of the specified {@link Ticker}.
     * 
     * @param ticker A target {@link Ticker} to update low price.
     * @param price A current price.
     */
    private void updateLowPrice(Ticker ticker, Num price) {
        if (price.isLessThan(ticker.current.lowPrice)) {
            ticker.current.lowPrice = price.doubleValue();

            for (Ticker upper : ticker.uppers) {
                updateLowPrice(upper, price);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
        for (Ticker ticker : tickers) {
            ticker.dispose();
        }
    }

    /**
     * Estimate the date range of full build.
     * 
     * @return
     */
    public DateRange estimateFullBuild() {
        Ticker ticker = on(Span.Day);
        long[] times = ticker.ticks.computeIdealSegmentTime();
        ZonedDateTime startCache = Chrono.utcBySeconds(times[0]);
        ZonedDateTime endCache = Chrono.utcBySeconds(times[1]);
        return DateRange.between(startCache, endCache).min(service.log.firstCacheDate()).max(service.log.lastCacheDate());
    }

    /**
     * Build ticker data from execution log.
     * 
     * @param forceRebuild
     * @return
     */
    public Signal<ZonedDateTime> buildFully(boolean forceRebuild) {
        return build(estimateFullBuild(), forceRebuild, false);
    }

    /**
     * Build ticker data from execution log by your specified date-range.
     * 
     * @param range
     * @param forceRebuild
     * @return
     */
    public Signal<ZonedDateTime> build(DateRange range, boolean forceRebuild) {
        return build(range, forceRebuild, false);
    }

    /**
     * Build ticker data from execution log by your specified date-range.
     * 
     * @param range
     * @param forceRebuild
     * @return
     */
    public Signal<ZonedDateTime> build(DateRange range, boolean forceRebuild, boolean forceRebuildLog) {
        range = range.min(service.log.firstCacheDate()).max(service.log.lastCacheDate()).maxByFuture(-1);

        Signal<ZonedDateTime> process = I.signal();
        if (forceRebuild) {
            process = buildCache(range, forceRebuildLog);
        } else {
            FeatherStore<Tick> ticks = on(Span.Day).ticks;
            long firstTime = ticks.firstTime();
            long lastTime = ticks.lastTime();

            if (firstTime == -1 && lastTime == -1) {
                process = buildCache(range, forceRebuildLog);
            } else {
                if (lastTime < range.end.toEpochSecond()) {
                    process = process.merge(buildCache(range.start(lastTime, SECONDS), forceRebuildLog));
                }

                if (range.start.toEpochSecond() < firstTime) {
                    process = process.merge(buildCache(range.end(firstTime, SECONDS), forceRebuildLog));
                }
            }
        }

        return process;
    }

    private Signal<ZonedDateTime> buildCache(DateRange range, boolean forceRebuildLog) {
        return range.days(false).effect(date -> {
            if (forceRebuildLog) {
                LogHouse house = service.loghouse();
                if (house.isValid()) {
                }
            }

            service.log.at(date, LogType.Fast)
                    .effectOnLifecycle(new TickerBuilder(new TickerManager(service, span -> on(span).ticks)))
                    .to(I.NoOP);
        });
    }

    /**
     * Clear all cache on memory.
     */
    public void clear() {
        for (Ticker ticker : tickers) {
            ticker.ticks.clear();
        }
    }
}