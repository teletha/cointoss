/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import kiss.Variable;

public abstract class AbstractNumberIndicator<N extends Number, Self extends AbstractNumberIndicator<N, Self>>
        extends AbstractIndicator<N, Self> {

    /**
     * 
     */
    protected AbstractNumberIndicator(Ticker ticker) {
        super(ticker);
    }

    /**
     * Wrap by exponetial moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    public abstract Self ema(int size);

    /**
     * Wrap by exponetial moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    public final Self ema(Variable<? extends Number> size) {
        return ema(size.v.intValue());
    }

    /**
     * Wrap by modified moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    public abstract Self mma(int size);

    /**
     * Wrap by modified moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    public final Self mma(Variable<? extends Number> size) {
        return mma(size.v.intValue());
    }

    /**
     * Wrap by simple moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    public abstract Self sma(int size);

    /**
     * Wrap by simple moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    public final Self sma(Variable<? extends Number> size) {
        return sma(size.v.intValue());
    }

    /**
     * Wrap by weighted moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    public abstract Self wma(int size);

    /**
     * Wrap by weighted moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    public final Self wma(Variable<? extends Number> size) {
        return wma(size.v.intValue());
    }

    /**
     * Gets an indicator that returns a number rounded to the specified number of decimal places.
     * 
     * @param size Scale A number of decimal places.
     * @return Rounded indicator.
     */
    public abstract Self scale(int size);
}