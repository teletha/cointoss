/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.math.BigDecimal;

import kiss.Variable;

public class Progressive {

    /** The root source. */
    private static final InheritableThreadLocal<Progressive> source = new InheritableThreadLocal();

    /** The title holder. */
    public final Variable<String> title = Variable.empty();

    /** The message holder. */
    public final Variable<String> message = Variable.empty();

    /** The total progression. */
    private double total;

    /** The current prgression. */
    private double current;

    /** The starting time. (epoch ms) */
    private long start;

    /**
     * Start with the given total progress.
     * 
     * @param total This must be positive. Negative or Zero will be ignored.
     * @return
     */
    public Progressive start(long total) {
        if (0 < total) {
            this.total = total;
            this.start = System.currentTimeMillis();
        }
        return this;
    }

    /**
     * Set the current progress.
     * 
     * @param current This must be positive. Negative or Zero will be ignored.
     * @return
     */
    public Progressive progress(long current) {
        if (0 < current) {
            this.current = current;
        }
        return this;
    }

    /**
     * Calculate the progress ratio.
     * 
     * @return
     */
    public BigDecimal ratio() {
        return ratio(0);
    }

    /**
     * Calculate the progress ratio with your scale.
     * 
     * @return
     */
    public BigDecimal ratio(int scale) {
        return new BigDecimal(current / total * 100d).setScale(scale);
    }

    /**
     * Get the contextual {@link Progressive}.
     * 
     * @return
     */
    public static Progressive lock() {
        // if (!Thread.currentThread().isVirtual()) {
        // throw new Error(Progressive.class.getSimpleName() + " works on virtual thread only.");
        // }

        Progressive now = source.get();
        if (now == null) {
            now = new Progressive();
            source.set(now);
        }
        return now;
    }
}