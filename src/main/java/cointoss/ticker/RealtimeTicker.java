/*
 * Copyright (C) 2020 cointoss Development Team
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

    private int longCount;

    private int longLosscutCount;

    private double longVolume;

    private double longLosscutVolume;

    private int shortCount;

    private int shortLosscutCount;

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

            longCount += latest.longCount - removed.longCount;
            longVolume += latest.longVolume - removed.longVolume;
            longLosscutCount += latest.longLosscutCount - removed.longLosscutCount;
            longLosscutVolume += latest.longLosscutVolume - removed.longLosscutVolume;
            shortCount += latest.shortCount - removed.shortCount;
            shortVolume += latest.shortVolume - removed.shortVolume;
            shortLosscutCount += latest.shortLosscutCount - removed.shortLosscutCount;
            shortLosscutVolume += latest.shortLosscutVolume - removed.shortLosscutVolume;

            latest = tick;
        });
    }

    public int longCount() {
        return longCount + latest.longCount();
    }

    public double longVolume() {
        return longVolume + latest.longVolume();
    }

    public int longLosscutCount() {
        return longLosscutCount + latest.longLosscutCount();
    }

    public double longLosscutVolume() {
        return longLosscutVolume + latest.longLosscutVolume();
    }

    public int shortCount() {
        return shortCount + latest.shortCount();
    }

    public double shortVolume() {
        return shortVolume + latest.shortVolume();
    }

    public int shortLosscutCount() {
        return shortLosscutCount + latest.shortLosscutCount();
    }

    public double shortLosscutVolume() {
        return shortLosscutVolume + latest.shortLosscutVolume();
    }
}