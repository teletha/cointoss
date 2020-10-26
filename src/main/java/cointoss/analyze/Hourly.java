/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.analyze;

import java.util.Objects;

import cointoss.MarketService;
import cointoss.ticker.Span;
import cointoss.ticker.TimeseriesStore;

public class Hourly {

    private final MarketService service;

    private final TimeseriesStore<Data> store = new TimeseriesStore<Data>(Span.Hour1, data -> data.time).disableMemorySaving();

    /**
     * Build.
     * 
     * @param service
     */
    public Hourly(MarketService service) {
        this.service = Objects.requireNonNull(service);
    }

    private void cache() {

    }

    private static class Data {

        private final long time;

        /**
         * @param time
         */
        private Data(long time) {
            this.time = time;
        }
    }
}
