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
import kiss.I;
import kiss.Variable;
import kiss.Ⅱ;

public class WaveTrendOscillator extends Indicator<Ⅱ<Num, Num>> {

    public final Variable<Integer> emaLength = Variable.of(10);

    public final Variable<Integer> lazyLength = Variable.of(21);

    private final Indicator<Num> wt1;

    public final Indicator<Num> wt2;

    /**
     * @param indicator
     */
    public WaveTrendOscillator(Ticker ticker) {
        super(ticker);

        Indicator<Num> ap = Indicator.build(ticker, Tick::typicalPrice);
        Indicator<Num> esa = ap.ema(emaLength.v);
        Indicator<Num> d = esa.map(ap, (a, b) -> a.minus(b).abs()).ema(emaLength.v);
        Indicator<Num> ci = ap.map(esa, d, (a, b, c) -> {
            if (c.isZero()) {
                return a.minus(b);
            }
            return a.minus(b).divide(Num.of(0.015).multiply(c));
        });
        wt1 = ci.ema(lazyLength.v);
        wt2 = wt1.sma(4);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Ⅱ<Num, Num> valueAt(Tick tick) {
        return I.pair(wt1.valueAt(tick), wt2.valueAt(tick));
    }
}
