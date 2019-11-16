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

import cointoss.util.Num;
import kiss.Ⅱ;

/**
 * Built-in {@link Indicator} collection.
 */
public class Indicators {

    public final static Indicator<Ⅱ<Num, Num>> waveTrend(Ticker ticker) {
        return waveTrend(ticker, 10, 21);
    }

    public final static Indicator<Ⅱ<Num, Num>> waveTrend(Ticker ticker, int channelLength, int averageLength) {
        Indicator<Num> ap = Indicator.build(ticker, Tick::typicalPrice);
        Indicator<Num> esa = ap.ema(channelLength);
        Indicator<Num> d = esa.map(ap, (a, b) -> a.minus(b).abs()).ema(channelLength);
        Indicator<Num> ci = ap.map(esa, d, (a, b, c) -> {
            if (c.isZero()) {
                return a.minus(b);
            }
            return a.minus(b).divide(Num.of(0.015).multiply(c));
        });
        Indicator<Num> wt1 = ci.ema(averageLength);
        Indicator<Num> wt2 = wt1.sma(4);
        return wt1.combine(wt2);
    }
}
