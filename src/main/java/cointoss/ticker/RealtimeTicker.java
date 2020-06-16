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

import cointoss.util.ring.RingBuffer;

public class RealtimeTicker {

    private Tick latest = Tick.EMPTY;

    private final RingBuffer<Tick> ticks;

    private int longCount;

    private double longVolume;

    private int shortCount;

    private double shortVolume;

    /**
     * @param tickerManager
     * @param size
     */
    RealtimeTicker(TickerManager tickerManager, int size) {
        ticks = new RingBuffer(size - 1);
        tickerManager.on(Span.Second5).open.to(tick -> {
            Tick removed = ticks.add(latest);
            if (removed == null) removed = Tick.EMPTY;

            longCount += latest.longCount - removed.longCount;
            longVolume += latest.longVolume - removed.longVolume;
            shortCount += latest.shortCount - removed.shortCount;
            shortVolume += latest.shortVolume - removed.shortVolume;

            latest = tick;
        });
    }

    public int longCount() {
        return longCount + latest.longCount();
    }

    public double longVolume() {
        return longVolume + latest.longVolume();
    }

    public int shortCount() {
        return shortCount + latest.shortCount();
    }

    public double shortVolume() {
        return shortVolume + latest.shortVolume();
    }
}
