/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import java.util.Objects;

import cointoss.MarketService;
import cointoss.execution.Execution;
import kiss.Disposable;
import kiss.WiseConsumer;
import kiss.WiseFunction;

public class TickerBuilder implements WiseFunction<Disposable, WiseConsumer<Execution>> {

    private final MarketService service;

    public TickerBuilder(MarketService service) {
        this.service = Objects.requireNonNull(service);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WiseConsumer<Execution> APPLY(Disposable disposer) throws Throwable {
        TickerManager temporary = new TickerManager(service);

        disposer.add(() -> {
            temporary.tickers().to(ticker -> {
                // update the close price by the latest price
                if (ticker.current != null) ticker.current.freeze();

                // save all ticks to disk
                ticker.ticks.commit();
            });
        });
        return temporary::update;
    }
}
