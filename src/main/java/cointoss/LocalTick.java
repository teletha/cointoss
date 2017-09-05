/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import eu.verdelhan.ta4j.Decimal;

/**
 * @version 2017/08/22 14:24:30
 */
class LocalTick {

    /** opening price */
    public Decimal opening;

    /** closing price */
    public Decimal closing;

    /** highest price */
    public Decimal highest = Decimal.ZERO;

    /** lowest price */
    public Decimal lowest = Decimal.MAX;

    /** target amount */
    public Decimal amount = Decimal.ZERO;

    /** target volume */
    public Decimal volume = Decimal.ZERO;

    /** target sell volume */
    public Decimal volumeSell = Decimal.ZERO;

    /** target buy volume */
    public Decimal volumeBuy = Decimal.ZERO;

    /** start time */
    public ZonedDateTime startTime;

    /** end time */
    public ZonedDateTime endTime;

    /**
     * Initialize.
     */
    LocalTick() {
        this.opening = this.closing = Decimal.ZERO;
        this.startTime = LocalDateTime.MIN.atZone(ZoneId.systemDefault());
        this.endTime = startTime.plusMinutes(1);
    }

    /**
     * Initialize.
     * 
     * @param opening
     */
    LocalTick(Execution opening) {
        this.opening = opening.price;
        this.startTime = opening.exec_date.withNano(0).withSecond(0);
        this.endTime = startTime.plusMinutes(1);

        mark(opening);
    }

    /**
     * Record {@link Execution}.
     * 
     * @param exe
     */
    void mark(Execution exe) {
        Decimal price = exe.price;
        this.closing = price;
        this.highest = Decimal.max(highest, price);
        this.lowest = Decimal.min(lowest, price);

        Decimal size = exe.size;
        this.volume = volume.plus(size);
        if (exe.side.isBuy()) {
            this.volumeBuy = volumeBuy.plus(size);
        } else {
            this.volumeSell = volumeSell.plus(size);
        }
    }

    /**
     * Calculate median.
     * 
     * @return
     */
    public Decimal getMedian() {
        return highest.plus(lowest).dividedBy(2);
    }

    /**
     * Calculate typical price.
     * 
     * @return
     */
    public Decimal getTypical() {
        return highest.plus(lowest).plus(closing).dividedBy(3);
    }

    /**
     * Calculate typical price.
     * 
     * @return
     */
    public Decimal getMiddle() {
        return opening.plus(closing).dividedBy(2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Tick [opening=" + opening + ", closing=" + closing + ", highest=" + highest + ", lowest=" + lowest + ", volume=" + volume + ", volumeSell=" + volumeSell + ", volumeBuy=" + volumeBuy + ", startTime=" + startTime + ", endTime=" + endTime + "]";
    }
}
