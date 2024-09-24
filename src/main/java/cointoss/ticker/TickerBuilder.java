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

    /** The temporary data holder. */
    private final TickerManager temporary;

    /**
     * Build with the specified service.
     * 
     * @param service
     */
    public TickerBuilder(MarketService service) {
        this(new TickerManager(service));
    }

    /**
     * Build with the specified data holder.
     * 
     * @param temporary
     */
    public TickerBuilder(TickerManager temporary) {
        this.temporary = temporary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WiseConsumer<Execution> APPLY(Disposable disposer) throws Throwable {
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
