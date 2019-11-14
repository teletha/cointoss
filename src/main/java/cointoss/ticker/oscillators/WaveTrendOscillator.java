/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker.oscillators;

import cointoss.ticker.Indicator;
import cointoss.ticker.Tick;
import cointoss.ticker.Ticker;
import cointoss.util.Num;

public class WaveTrendOscillator {

    public final Indicator wt1;

    public final Indicator wt2;

    /**
     * @param ticker
     */
    public WaveTrendOscillator(Ticker ticker) {
        Indicator ap = Indicator.build(ticker, Tick::typicalPrice);
        Indicator esa = ap.ema(10);
        Indicator d = esa.calculate(ap, (a, b) -> a.minus(b).abs()).ema(10);
        Indicator ci = ap.calculate(esa, d, (a, b, c) -> a.minus(b).divide(Num.of(0.015).multiply(c)));
        wt1 = ci.ema(21);
        wt2 = wt1.sma(4);
    }
}
