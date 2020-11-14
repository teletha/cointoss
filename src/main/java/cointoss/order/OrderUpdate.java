/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.order;

import java.util.Objects;
import java.util.function.Consumer;

import cointoss.Direction;
import cointoss.util.arithmetic.Num;

public class OrderUpdate {

    /** The update action. */
    final Consumer<OrderManager> updater;

    /**
     * Build generic updater.
     * 
     * @param updater
     */
    private OrderUpdate(Consumer<OrderManager> updater) {
        this.updater = Objects.requireNonNull(updater);
    }

    public static OrderUpdate cancell(String id) {
        return new OrderUpdate(m -> m.cancel(id));
    }

    public static OrderUpdate create(String id, Direction side, Num size, Num price) {
        return new OrderUpdate(m -> m.create(id, side, size, price));
    }

    public static OrderUpdate executing(String id, Num size, Num price) {
        return new OrderUpdate(m -> m.execute(id, size, price));
    }
}
