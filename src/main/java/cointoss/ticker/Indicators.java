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

import kiss.Ⅱ;

/**
 * Built-in {@link Indicator} collection.
 */
public class Indicators {

    public final static Indicator<Ⅱ<Double, Double>> waveTrend(Ticker ticker) {
        return waveTrend(ticker, 10, 21);
    }

    public final static Indicator<Ⅱ<Double, Double>> waveTrend(Ticker ticker, int channelLength, int averageLength) {
        DoubleIndicator ap = DoubleIndicator.build(ticker, tick -> tick.typicalPrice().doubleValue());
        DoubleIndicator esa = ap.ema(channelLength);
        DoubleIndicator d = esa.dmap(ap, (a, b) -> Math.abs(a - b)).ema(channelLength);
        DoubleIndicator ci = ap.dmap(esa, d, (a, b, c) -> {
            if (c == 0d) {
                return a - b;
            }
            return (a - b) / 0.015 * c;
        });
        DoubleIndicator wt1 = ci.ema(averageLength).scale(2);
        DoubleIndicator wt2 = wt1.sma(4).scale(2);
        return wt1.combine(wt2);
    }
}
