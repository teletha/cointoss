/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.indicator.simple;

import java.util.function.Function;

import cointoss.Amount;
import cointoss.Tick;
import cointoss.TimeSeries;
import cointoss.indicator.AbstractIndicator;

/**
 * @version 2017/08/25 22:47:25
 */
public class PriceIndicator extends AbstractIndicator<Amount> {

    private static final long serialVersionUID = 8147315274637238723L;

    private final Function<Tick, Amount> target;

    /**
     * @param series
     */
    public PriceIndicator(TimeSeries series, Function<Tick, Amount> target) {
        super(series);
        this.target = target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Amount calculate(int index) {
        return target.apply(series.getTick(index));
    }
}
