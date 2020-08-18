/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListMap;

import cointoss.execution.Execution;
import cointoss.util.Num;

public class PriceRangedVolumeManager {

    private final ConcurrentSkipListMap<Long, PriceRangedVolume> volumes = new ConcurrentSkipListMap(Comparator.reverseOrder());

    private PriceRangedVolume volume;

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
        volume = new PriceRangedVolume(tick.openTime, tick.openPrice, priceRange, scale);
        volumes.put(tick.openTime, volume);
    }

    public void update(Execution e) {
        volume.update(e.price, e.size.doubleValue());
    }

    public PriceRangedVolume latest() {
        return volume;
    }

    public Collection<PriceRangedVolume> all() {
        return volumes.values();
    }
}
