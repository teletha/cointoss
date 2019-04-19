/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.analyze;

import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * @version 2017/08/30 20:45:02
 */
public class LongSummary {

    /** MAX value. */
    public long min = Long.MAX_VALUE;

    /** MIN value. */
    public long max = Long.MIN_VALUE;

    /** Total value. */
    public long total = 0;

    /** Number of values. */
    public int size = 0;

    /**
     * Add new value to summarize.
     * 
     * @param value
     */
    public void add(long value) {
        min = Math.min(min, value);
        max = Math.max(max, value);
        total += value;
        size++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "最小" + duration(min) + "\t最大" + duration(max) + "\t平均" + duration(total / Math.max(1, size)) + "\r\n";
    }

    /**
     * Format duration.
     * 
     * @param time seconds
     * @return
     */
    private String duration(long time) {
        if (time == Long.MAX_VALUE) {
            return "";
        }
        return DurationFormatUtils.formatDuration(time * 1000, "HH:mm:ss");
    }
}
