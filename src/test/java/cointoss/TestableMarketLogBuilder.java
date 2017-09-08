/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.time.LocalDate;

import kiss.Signal;

/**
 * @version 2017/09/08 17:51:59
 */
public class TestableMarketLogBuilder implements MarketLogBuilder {

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> initialize() {
        return Signal.EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> from(LocalDate start) {
        return Signal.EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDate getCacheStart() {
        return LocalDate.MIN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDate getCacheEnd() {
        return LocalDate.MIN;
    }
}
