/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.volume;

import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListMap;

import cointoss.Direction;
import cointoss.execution.Execution;
import cointoss.ticker.Tick;
import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.Signal;

public class PriceRangedVolumeManager {

    /** The time-based cache. */
    private final ConcurrentSkipListMap<Long, PriceRangedVolumePeriod[]> volumes = new ConcurrentSkipListMap(Comparator.reverseOrder());

    /** The volume for buyers. */
    private PriceRangedVolumePeriod buyer;

    /** The volume for sellers. */
    private PriceRangedVolumePeriod seller;

    /** The minimum price range. */
    private final Num priceRange;

    /**
     * @param service
     */
    public PriceRangedVolumeManager(Num priceRange) {
        this.priceRange = priceRange;
    }

    /**
     * End the current record and start a new one.
     * 
     * @param tick A starting point.
     */
    public void start(Tick tick) {
        start(tick.openTime, tick.openPrice);
    }

    /**
     * End the current record and start a new one.
     * 
     * @param startTime A starting time.
     * @param startPrice A starting price.
     */
    public void start(long startTime, Num startPrice) {
        buyer = new PriceRangedVolumePeriod(startTime, startPrice, priceRange);
        seller = new PriceRangedVolumePeriod(startTime, startPrice, priceRange);
        volumes.put(startTime, new PriceRangedVolumePeriod[] {buyer, seller});
    }

    /**
     * Update the current record.
     * 
     * @param e
     */
    public void update(Execution e) {
        if (e.direction == Direction.BUY) {
            buyer.update(e.price, e.size.floatValue());
        } else {
            seller.update(e.price, e.size.floatValue());
        }
    }

    /**
     * Retrieve the latest record.
     * 
     * @return
     */
    public PriceRangedVolumePeriod[] latest() {
        return new PriceRangedVolumePeriod[] {buyer, seller};
    }

    /**
     * Retrieve all past records (without the latest).
     * 
     * @return
     */
    public Signal<PriceRangedVolumePeriod[]> past() {
        return I.signal(volumes.values()).skip(1);
    }
}