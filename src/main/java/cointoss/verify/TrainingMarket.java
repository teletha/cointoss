/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.verify;

import cointoss.Market;

public class TrainingMarket extends Market {

    /** The backend original market. */
    public final Market backend;

    /**
     * @param backend
     */
    public TrainingMarket(Market backend) {
        super(new TrainingMarketService(backend.service));
        this.backend = backend;

        service.executionsRealtimely().to(e -> {
            ((TrainingMarketService) service).frontend.emulate(e, timelineObservers);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return backend.toString() + " - DEMO";
    }
}
