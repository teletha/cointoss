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

import cointoss.MarketService;
import cointoss.execution.Execution;
import kiss.Disposable;
import kiss.WiseConsumer;
import kiss.WiseFunction;

public class TickerBuilder implements WiseFunction<Disposable, WiseConsumer<Execution>> {

    private final MarketService service;

    /**
     * @param service
     */
    public TickerBuilder(MarketService service) {
        this.service = service;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WiseConsumer<Execution> APPLY(Disposable param) throws Throwable {
        TickerManager temporary = new TickerManager(service);

        param.add(() -> {
            temporary.tickers().to(ticker -> {
                // update the close price by the latest price
                ticker.current.freeze();

                // save all ticks to disk
                ticker.ticks.commit();
            });
        });
        return temporary::update;
    }
}