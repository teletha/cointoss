/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import cointoss.util.ring.RingBuffer;

public class RealtimeTicker {

    private Tick latest = Tick.EMPTY;

    private final RingBuffer<Tick> ticks;

    private double longVolume;

    private double longLosscutVolume;

    private double shortVolume;

    private double shortLosscutVolume;

    /**
     * @param tickerManager
     * @param size
     */
    RealtimeTicker(TickerManager tickerManager, int size) {
        ticks = new RingBuffer(size - 1);
        tickerManager.on(Span.Second5).open.to(tick -> {
            Tick removed = ticks.add(latest);
            if (removed == null) removed = Tick.EMPTY;

            longVolume += latest.longVolume - removed.longVolume;
            longLosscutVolume += latest.longLosscutVolume - removed.longLosscutVolume;
            shortVolume += latest.shortVolume - removed.shortVolume;
            shortLosscutVolume += latest.shortLosscutVolume - removed.shortLosscutVolume;

            latest = tick;
        });
    }

    public double longVolume() {
        return longVolume + latest.longVolume();
    }

    public double longLosscutVolume() {
        return longLosscutVolume + latest.longLosscutVolume();
    }

    public double shortVolume() {
        return shortVolume + latest.shortVolume();
    }

    public double shortLosscutVolume() {
        return shortLosscutVolume + latest.shortLosscutVolume();
    }
}