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

/**
 * @version 2017/08/22 14:24:30
 */
public class Tick {

    /** opening price */
    public Amount opening;

    /** closing price */
    public Amount closing;

    /** highest price */
    public Amount highest = Amount.ZERO;

    /** lowest price */
    public Amount lowest = Amount.MAX;

    /** target amount */
    public Amount amount = Amount.ZERO;

    /** target volume */
    public Amount volume = Amount.ZERO;

    /** target sell volume */
    public Amount volumeSell = Amount.ZERO;

    /** target buy volume */
    public Amount volumeBuy = Amount.ZERO;

    /** start time */
    public LocalDateTime startTime;

    /** end time */
    public LocalDateTime endTime;

    /**
     * Initialize.
     */
    Tick() {
        this.opening = this.closing = Amount.ZERO;
        this.startTime = LocalDateTime.MIN;
        this.endTime = startTime.plusMinutes(1);
    }

    /**
     * Initialize.
     * 
     * @param opening
     */
    Tick(Execution opening) {
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
        Amount price = exe.price;
        this.closing = price;
        this.highest = Amount.max(highest, price);
        this.lowest = Amount.min(lowest, price);

        Amount size = exe.size;
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
    public Amount getMedian() {
        return highest.plus(lowest).divide(2);
    }

    /**
     * Calculate typical price.
     * 
     * @return
     */
    public Amount getTypical() {
        return highest.plus(lowest).plus(closing).divide(3);
    }

    /**
     * Calculate typical price.
     * 
     * @return
     */
    public Amount getMiddle() {
        return opening.plus(closing).divide(2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Tick [opening=" + opening + ", closing=" + closing + ", highest=" + highest + ", lowest=" + lowest + ", volume=" + volume + ", volumeSell=" + volumeSell + ", volumeBuy=" + volumeBuy + ", startTime=" + startTime + ", endTime=" + endTime + "]";
    }
}
