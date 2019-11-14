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

import java.util.Objects;

public abstract class AbstractIndicator implements Indicator {

    /** The target {@link Ticker}. */
    protected final Ticker ticker;

    /** The wrapped {@link Indicator}. (OPTIONAL: may be null) */
    protected final Indicator wrapped;

    /**
     * Build with the target {@link Ticker}.
     * 
     * @param ticker A target ticker.
     */
    protected AbstractIndicator(Ticker ticker) {
        this.ticker = Objects.requireNonNull(ticker);
        this.wrapped = null;
    }

    /**
     * Build with the delegation {@link Indicator}.
     * 
     * @param indicator A {@link Indicator} to delegate.
     */
    protected AbstractIndicator(Indicator indicator) {
        this.wrapped = Objects.requireNonNull(indicator);
        this.ticker = Objects.requireNonNull(indicator.ticker());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Ticker ticker() {
        return ticker;
    }
}
