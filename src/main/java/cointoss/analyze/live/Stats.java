/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.analyze.live;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public class Stats implements Serializable {

    public final String name;

    public final int decays;

    public final long n;

    public final Double decayedN;

    public final Double min;

    public final Double decayedMin;

    public final Double max;

    public final Double decayedMax;

    public final Double mean;

    public final Double variance;

    public final Double skewness;

    public final Double kurtosis;

    public final Map<Double, Double> quantiles;

    public Stats(final String name, final LiveStats stats) {
        this.name = name;
        n = stats.num();
        min = specialFloatsToNull(name, "min", stats.minimum());
        max = specialFloatsToNull(name, "max", stats.maximum());
        mean = specialFloatsToNull(name, "mean", stats.mean());
        variance = specialFloatsToNull(name, "variance", stats.variance());
        skewness = specialFloatsToNull(name, "skewness", stats.skewness());
        kurtosis = specialFloatsToNull(name, "kurtosis", stats.kurtosis());
        quantiles = Collections.unmodifiableMap(stats.quantiles());
        decayedMin = specialFloatsToNull(name, "decayedMin", stats.decayedMinimum());
        decayedMax = specialFloatsToNull(name, "decayedMax", stats.decayedMaximum());
        decayedN = specialFloatsToNull(name, "decayedN", stats.decayedNum());
        decays = stats.decayCount();
    }

    public Stats(final String name, final long n, final double min, final double max, final double mean, final double variance, final double skewness, final double kurtosis, final Map<Double, Double> quantiles) {
        this.name = name;
        this.decays = 0;
        this.n = n;
        this.decayedN = (double) n;
        this.min = min;
        this.decayedMin = min;
        this.max = max;
        this.decayedMax = max;
        this.mean = mean;
        this.variance = variance;
        this.skewness = skewness;
        this.kurtosis = kurtosis;
        this.quantiles = Collections.unmodifiableMap(quantiles);
    }

    private static Double specialFloatsToNull(final String name, final String var, final double value) {
        if (Double.isNaN(value)) {
            return null;
        }
        if (Double.isInfinite(value)) {
            return null;
        }
        return value;
    }
}