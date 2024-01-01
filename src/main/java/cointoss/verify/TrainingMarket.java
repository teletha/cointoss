/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.verify;

import cointoss.Market;
import cointoss.orderbook.OrderBookManager;
import cointoss.ticker.TickerManager;
import cointoss.volume.PriceRangedVolumeManager;

public class TrainingMarket extends Market {

    /** The backend original market. */
    public final Market backend;

    /**
     * @param backend
     */
    public TrainingMarket(Market backend) {
        super(new TrainingMarketService(backend));
        this.backend = backend;

        service.executionsRealtimely().to(e -> {
            ((TrainingMarketService) service).frontend.emulate(e, timelineObservers);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected OrderBookManager createOrderBookManager() {
        return ((TrainingMarketService) service).market.orderBook;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PriceRangedVolumeManager createPriceRangedVolumeManager() {
        return ((TrainingMarketService) service).market.priceVolume;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TickerManager createTickerManager() {
        return ((TrainingMarketService) service).market.tickers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return backend.toString() + " - DEMO";
    }
}