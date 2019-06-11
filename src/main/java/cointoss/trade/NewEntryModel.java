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

import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.order.Order;
import icy.manipulator.Icy;

@Icy
public abstract class NewEntryModel implements Directional {

    @Icy.Property
    abstract Trader trader();

    @Icy.Property
    abstract Order order();

    /**
     * {@inheritDoc}
     */
    @Override
    public Direction direction() {
        return order().direction;
    }

    public void cancelAfter(int time, ChronoUnit unit) {

    }

    public void set(StopStrategy loss) {

    }

    public void exitLimit(Order exitOrder, Consumer<Order> exit) {

    }

    public void log(String message, Object... params) {
        order().log(message, params);
    }
}
