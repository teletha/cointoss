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

import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;

import cointoss.util.Num;

public abstract class AbstractCachedIndicator extends AbstractIndicator {

    /** CACHE */
    private final MutableIntObjectMap<Num> cache = IntObjectMaps.mutable.empty();

    /**
     * @see AbstractIndicator#AbstractIndicator(Ticker)
     */
    protected AbstractCachedIndicator(Ticker ticker) {
        super(ticker);
    }

    /**
     * @see AbstractIndicator#AbstractIndicator(Indicator)
     */
    protected AbstractCachedIndicator(Indicator indicator) {
        super(indicator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num valueAt(int index) {
        return cache.getIfAbsentPut(index, () -> calculate(index));
    }

    /**
     * Calculate actual value.
     * 
     * @param index The tick index.
     * @return A calcualted value.
     */
    protected abstract Num calculate(int index);
}
