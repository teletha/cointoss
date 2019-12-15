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

import java.util.function.Function;

import kiss.Variable;

public abstract class NumberIndicatable<N extends Number, Self extends NumberIndicatable<N, Self>> extends Indicatable<N> {

    /**
     * 
     */
    protected NumberIndicatable() {
        super();
    }

    /**
     * @param normalizer
     */
    protected NumberIndicatable(Function<Tick, Tick> normalizer) {
        super(normalizer);
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
}
