/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade;

import java.util.function.Consumer;

import cointoss.Directional;
import cointoss.Market;
import cointoss.order.OrderStrategy.Makable;
import cointoss.order.OrderStrategy.Orderable;
import cointoss.order.OrderStrategy.Takable;
import cointoss.util.arithmetic.Num;

public interface TradingEntry {

    /**
     * The current market.
     * 
     * @return
     */
    Market market();

    /**
     * We will order with the specified quantity. Use the return the {@link Takable} &
     * {@link Makable} value to define the details of the ordering method.
     * 
     * @param directional This entry's direction.
     * @param size This entry's size.
     * @return Chainable API.
     */
    default Scenario entry(Directional directional, long size) {
        return entry(directional, size, Orderable::take);
    }

    /**
     * We will order with the specified quantity. Use the return the {@link Takable} &
     * {@link Makable} value to define the details of the ordering method.
     * 
     * @param directional This entry's direction.
     * @param size This entry's size.
     * @param declaration This entry's order strategy.
     * @return Chainable API.
     */
    default Scenario entry(Directional directional, long size, Consumer<Orderable> declaration) {
        return entry(directional, Num.of(size), declaration);
    }

    /**
     * We will order with the specified quantity. Use the return the {@link Takable} &
     * {@link Makable} value to define the details of the ordering method.
     * 
     * @param directional This entry's direction.
     * @param size This entry's size.
     * @return Chainable API.
     */
    default Scenario entry(Directional directional, double size) {
        return entry(directional, size, Orderable::take);
    }

    /**
     * We will order with the specified quantity. Use the return the {@link Takable} &
     * {@link Makable} value to define the details of the ordering method.
     * 
     * @param directional This entry's direction.
     * @param size This entry's size.
     * @param declaration This entry's order strategy.
     * @return Chainable API.
     */
    default Scenario entry(Directional directional, double size, Consumer<Orderable> declaration) {
        return entry(directional, Num.of(size), declaration);
    }

    /**
     * We will order with the specified quantity. Use the return the {@link Takable} &
     * {@link Makable} value to define the details of the ordering method.
     * 
     * @param directional This entry's direction.
     * @param size This entry's size.
     * @return Chainable API.
     */
    default Scenario entry(Directional directional, Num size) {
        return entry(directional, size, Orderable::take);
    }

    /**
     * We will order with the specified quantity. Use the return the {@link Takable} &
     * {@link Makable} value to define the details of the ordering method.
     * 
     * @param directional This entry's direction.
     * @param size This entry's size.
     * @param declaration This entry's order strategy.
     * @return Chainable API.
     */
    Scenario entry(Directional directional, Num size, Consumer<Orderable> declaration);
}