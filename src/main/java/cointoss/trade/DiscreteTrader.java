/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade;

import java.util.function.Consumer;

import cointoss.Directional;
import cointoss.Market;
import cointoss.order.OrderStrategy.Makable;
import cointoss.order.OrderStrategy.Orderable;
import cointoss.order.OrderStrategy.Takable;
import cointoss.util.arithmetic.Num;
import kiss.I;

public class DiscreteTrader extends Trader {

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void declare(Market market, Funds fund) {
    }

    /**
     * We will order with the specified quantity. Use the return the {@link Takable} &
     * {@link Makable} value to define the details of the ordering method.
     * 
     * @param <S> Ordering interface
     * @param size A entry size.
     * @return A ordering method.
     */
    public final DiscreteScenario entry(Directional directional, Num size) {
        return entry(directional, size, Orderable::take);
    }

    /**
     * We will order with the specified quantity. Use the return the {@link Takable} &
     * {@link Makable} value to define the details of the ordering method.
     * 
     * @param <S> Ordering interface
     * @param size A entry size.
     * @return A ordering method.
     */
    public final DiscreteScenario entry(Directional directional, Num size, Consumer<Orderable> declaration) {
        DiscreteScenario scenario = new DiscreteScenario();
        when(I.signal(0), v -> scenario);
        scenario.entry(directional, size, declaration);
        return scenario;
    }
}
