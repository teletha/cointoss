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

import java.util.function.IntFunction;

import cointoss.Amount;
import cointoss.indicator.AbstractIndicator;
import cointoss.indicator.Indicator;

/**
 * @version 2017/08/26 1:16:13
 */
@SuppressWarnings("serial")
public abstract class AbstractPeriodicalDelegationIndicator extends AbstractIndicator<Amount> {

    /** The delegator indicator. */
    protected final Indicator<Amount> delegator;

    /** The value calculator. */
    protected IntFunction<Amount> calculator;

    /**
     * @param indicator
     * @param period
     */
    protected AbstractPeriodicalDelegationIndicator(Indicator<Amount> indicator, int period) {
        super(indicator);

        this.delegator = indicator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Amount calculate(int index) {
        return calculator.apply(index);
    }
}
