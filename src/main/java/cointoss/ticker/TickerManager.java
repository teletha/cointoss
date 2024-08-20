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

import java.time.ZonedDateTime;

import cointoss.Direction;
import cointoss.Market;
import cointoss.MarketService;
import cointoss.execution.Execution;
import cointoss.execution.LogType;
import cointoss.util.Chrono;
import cointoss.util.JobType;
import cointoss.util.feather.FeatherStore;
import hypatia.Num;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import kiss.Variable;

public final class TickerManager implements Disposable {

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
        this(null);
    }

    /**
     * Create {@link TickerManager}.
     */
    public TickerManager(MarketService service) {
        this.service = service;

        for (int i = size - 1; 0 <= i; i--) {
            Ticker ticker = tickers[i] = new Ticker(Span.values()[i], this);

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
     * Freeze the latest tick and save all ticks to disk.
     */
    public void freeze() {
        for (Ticker ticker : tickers) {
            // update the close price by the latest price
            ticker.current.freeze();

            // save all ticks to disk
            ticker.ticks.commit();
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
     * Build ticker data by date range.
     * 
     * @param start
     * @param end
     * @param forceRebuild
     */
    public void build(ZonedDateTime start, ZonedDateTime end, boolean forceRebuild) {
        if (forceRebuild) {
            buildCache(start, end);
        } else {
            FeatherStore<Tick> current = on(Span.Day).ticks;
            Tick first = current.first();
            Tick last = current.last();

            if (first == null && last == null) {
                buildCache(start, end);
            } else {
                if (start == null && last != null) {
                    start = last.date();
                }

                if (end == null && first != null) {
                    end = first.date();
                }

                if (start.toEpochSecond() < current.firstTime()) {
                    buildCache(start, current.first().date());
                }

                if (current.lastTime() < end.toEpochSecond()) {
                    buildCache(current.last().date(), end);
                }
            }
        }
    }

    private void buildCache(ZonedDateTime start, ZonedDateTime end) {
        I.info("Building ticker data on " + service + " from " + start + " to " + end + ".");

        TickerManager temporary = new TickerManager(service);
        service.log.range(start, end, LogType.Fast).to(temporary::update, e -> {
        }, () -> {
            temporary.freeze();

            for (Ticker ticker : tickers) {
                ticker.ticks.updateMeta();
            }
        });
    }

    /**
     * Generates data for the Ticker cache.
     * 
     * @param startDay
     */
    public void buildDiskCacheFrom(ZonedDateTime startDay) {
        JobType.TickerGeneration.schedule(service, process -> {
            TickerManager temporary = new TickerManager(service);
            FeatherStore<Tick> ticks = on(Span.Day).ticks;

            ZonedDateTime start = Chrono.max(startDay, service.log.firstCacheDate());
            ZonedDateTime end = ticks.firstCache().date();

            if (ticks.firstTime() <= start.toEpochSecond()) {
                System.out.println("END " + ticks.first() + "  " + start + "  " + on(Span.Day).ticks);
                return;
            } else {
                System.out.println("NOOOOOOO " + ticks.first() + "  " + start + "  " + on(Span.Day).ticks);
            }

            temporary.on(Span.Day).open.to(e -> {
                System.out.println(service + " converts log to ticker. [" + e.date() + "]");
            });

            service.log.range(start, end, LogType.Fast).to(temporary::update, e -> {
            }, () -> {
                temporary.freeze();
            });
        });
    }

}