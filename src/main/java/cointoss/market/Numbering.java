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

    private final boolean milliBase;

    /**
     * @param padding
     */
    public Numbering(boolean milliBase, long padding) {
        this.padding = padding;
        this.milliBase = milliBase;
    }

    /**
     * Calculate epoch time from ID.
     * 
     * @param id A target ID.
     * @return Epoch seconds.
     */
    public long decode(long id) {
        return id / padding;
    }

    /**
     * Calculate ID from date-time.
     * 
     * @param time A target time.
     * @return The computed ID.
     */
    public long encode(ZonedDateTime time) {
        long epoch = milliBase ? time.toInstant().toEpochMilli() : time.toEpochSecond();
        return epoch * padding;
    }
}
