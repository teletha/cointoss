/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.temporal.ChronoField;

import cointoss.Execution;
import cointoss.util.Num;
import kiss.I;
import kiss.Signal;

/**
 * @version 2018/06/30 1:22:20
 */
public class TickerManager {

    /** The start index for 'second' type {@link TickSpan}. */
    private static final int SecondSpanStart = TickSpan.index(ChronoField.SECOND_OF_MINUTE, true);

    /** The end index for 'second' type {@link TickSpan}. */
    private static final int SecondSpanEnd = TickSpan.index(ChronoField.SECOND_OF_MINUTE, false);

    /** The start index for 'minute' type {@link TickSpan}. */
    private static final int MinuteSpanStart = TickSpan.index(ChronoField.MINUTE_OF_HOUR, true);

    /** The end index for 'minute' type {@link TickSpan}. */
    private static final int MinuteSpanEnd = TickSpan.index(ChronoField.MINUTE_OF_HOUR, false);

    /** The start index for 'hour' type {@link TickSpan}. */
    private static final int HourSpanStart = TickSpan.index(ChronoField.HOUR_OF_DAY, true);

    /** The end index for 'hour' type {@link TickSpan}. */
    private static final int HourSpanEnd = TickSpan.index(ChronoField.HOUR_OF_DAY, false);

    /** The start index for 'day' type {@link TickSpan}. */
    private static final int DaySpanStart = TickSpan.index(ChronoField.EPOCH_DAY, true);

    /** The end index for 'day' type {@link TickSpan}. */
    private static final int DaySpanEnd = TickSpan.index(ChronoField.EPOCH_DAY, false);

    /** The base tick. */
    private final BaseStatistics base = new BaseStatistics();

    /** The number of tickers. */
    private final int size = TickSpan.values().length;

    /** The actual tickers. */
    private final Ticker2[] tickers = new Ticker2[size];

    /**
     * 
     */
    public TickerManager() {
        TickSpan[] spans = TickSpan.values();

        for (int i = 0; i < spans.length; i++) {
            tickers[i] = new Ticker2(spans[i]);
        }

        for (Field f : getClass().getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) {
                try {
                    System.out.println(f + "  " + f.get(null));
                } catch (IllegalArgumentException e) {
                    throw I.quiet(e);
                } catch (IllegalAccessException e) {
                    throw I.quiet(e);
                }
            }
        }
    }

    /**
     * Retrieve the {@link Ticker2} by {@link TickSpan}.
     * 
     * @param span The target {@link TickSpan}.
     */
    public Ticker2 tickerBy(TickSpan span) {
        return tickers[span.ordinal()];
    }

    /**
     * Retrieve all {@link Ticker2}s.
     * 
     * @return
     */
    public Signal<Ticker2> tickers() {
        return I.signal(tickers);
    }

    /**
     * Update tick.
     * 
     * @param execution
     */
    public void update(Execution execution) {
        Num price = execution.price;

        // optimize ticker update
        if (tickers[0].update(execution, base)) {
            for (int seconds = 1; seconds <= 2; seconds++) {
                tickers[seconds].update(execution, base);
            }

            if (tickers[3].update(execution, base)) {
                for (int minutes = 4; minutes <= 8; minutes++) {
                    tickers[minutes].update(execution, base);
                }

                if (tickers[9].update(execution, base)) {
                    for (int minutes = 10; minutes <= 15; minutes++) {
                        tickers[minutes].update(execution, base);
                    }

                    if (tickers[15].update(execution, base)) {
                        for (int minutes = 16; minutes <= 18; minutes++) {
                            tickers[minutes].update(execution, base);
                        }
                    }
                }
            }
        }

        // update base
        base.update(execution);

        // Confirm that the high price is updated in order from the top ticker.
        // If there is an update, it is considered that all tickers below it are updated as well.
        for (int i = 0; i < size; i++) {
            Tick tick = tickers[i].last;

            if (price.isGreaterThan(tick.highPrice)) {
                tick.highPrice = price;
            } else {
                break;
            }
        }

        for (int i = 0; i < size; i++) {
            Tick tick = tickers[i].last;

            if (price.isLessThan(tick.lowPrice)) {
                tick.lowPrice = price;
            } else {
                break;
            }
        }
    }
}
