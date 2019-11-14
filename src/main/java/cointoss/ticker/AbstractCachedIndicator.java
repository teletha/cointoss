/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentSkipListMap;

import cointoss.util.Num;

public abstract class AbstractCachedIndicator extends Indicator {

    /** CACHE */
    private final ConcurrentSkipListMap<ZonedDateTime, Num> cache = new ConcurrentSkipListMap();

    /**
     * @see Indicator#Indicator(Ticker)
     */
    protected AbstractCachedIndicator(Ticker ticker) {
        super(ticker);
    }

    /**
     * @see Indicator#Indicator(Indicator)
     */
    protected AbstractCachedIndicator(Indicator indicator) {
        super(indicator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Num valueAt(Tick tick) {
        return cache.computeIfAbsent(tick.start, key -> calculate(tick));
    }

    /**
     * Calculate actual value.
     * 
     * @param tick The target tick.
     * @return A calcualted value.
     */
    protected abstract Num calculate(Tick tick);
}
