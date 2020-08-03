/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market;

import java.time.ZonedDateTime;

public class Numbering {

    public final long padding;

    /**
     * @param padding
     */
    public Numbering(long padding) {
        this.padding = padding;
    }

    /**
     * Calculate epoch milliseconds from ID.
     * 
     * @param id A target ID.
     * @return Epoch milliseconds.
     */
    public long mills(long id) {
        return id / padding;
    }

    /**
     * Calculate epoch seconds from ID.
     * 
     * @param id A target ID.
     * @return Epoch seconds.
     */
    public long secs(long id) {
        return id / (padding * 1000);
    }

    /**
     * Calculate ID from epoch milliseconds.
     * 
     * @param millis A target time.
     * @return The computed ID.
     */
    public long fromMilli(long millis) {
        return millis * padding;
    }

    /**
     * Calculate ID from epoch seconds.
     * 
     * @param secs A target time.
     * @return The computed ID.
     */
    public long fromSec(long secs) {
        return secs * padding * 1000;
    }

    /**
     * Calculate ID from date-time.
     * 
     * @param time A target time.
     * @return The computed ID.
     */
    public long fromTimeToMill(ZonedDateTime time) {
        return fromMilli(time.toInstant().toEpochMilli());
    }

    /**
     * Calculate ID from date-time.
     * 
     * @param time A target time.
     * @return The computed ID.
     */
    public long fromTimeToSec(ZonedDateTime time) {
        System.out.println(time.toEpochSecond());
        return fromSec(time.toEpochSecond());
    }
}
