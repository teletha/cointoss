/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.volume;

import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListMap;

import cointoss.Direction;
import cointoss.execution.Execution;
import cointoss.ticker.Tick;
import cointoss.util.Num;
import kiss.I;
import kiss.Signal;

public class PriceRangedVolumeManager {

    private final ConcurrentSkipListMap<Long, PriceRangedVolume[]> volumes = new ConcurrentSkipListMap(Comparator.reverseOrder());

    private PriceRangedVolume longs;

    private PriceRangedVolume shorts;

    private final Num priceRange;

    private final int scale;

    /**
     * @param service
     */
    public PriceRangedVolumeManager(Num priceRange, int scale) {
        this.priceRange = priceRange;
        this.scale = scale;
    }

    public void update(Tick tick) {
        longs = new PriceRangedVolume(tick.openTime, tick.openPrice, priceRange, scale);
        shorts = new PriceRangedVolume(tick.openTime, tick.openPrice, priceRange, scale);
        volumes.put(tick.openTime, new PriceRangedVolume[] {longs, shorts});
    }

    public void update(Execution e) {
        if (e.direction == Direction.BUY) {
            longs.update(e.price, e.size.doubleValue());
        } else {
            shorts.update(e.price, e.size.doubleValue());
        }
    }

    public PriceRangedVolume[] latest() {
        return new PriceRangedVolume[] {longs, shorts};
    }

    public Signal<PriceRangedVolume[]> previous() {
        return I.signal(volumes.values()).skip(1);
    }
}
