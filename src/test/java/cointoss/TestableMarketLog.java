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

import java.nio.file.Path;
import java.time.ZonedDateTime;

import filer.Filer;
import kiss.Signal;

/**
 * @version 2017/09/08 17:51:59
 */
public class TestableMarketLog implements MarketLog {

    /**
     * {@inheritDoc}
     */
    @Override
    public Path cacheRoot() {
        return Filer.locateTemporary();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> from(ZonedDateTime start) {
        return Signal.EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTime getCacheStart() {
        return ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, Execution.UTC);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTime getCacheEnd() {
        return ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, Execution.UTC);
    }
}
