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

    protected final Ticker ticker;

    /**
     * @param ticker
     */
    protected AbstractIndicator(Ticker ticker) {
        this.ticker = Objects.requireNonNull(ticker);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Ticker ticker() {
        return ticker;
    }
}
