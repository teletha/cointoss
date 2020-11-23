/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order.strategy;

import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

public interface OrderStrategy {

    /**
     * Make stop order with waiting time.
     * 
     * @param time
     * @param unit
     * @return
     */
    static Consumer<Orderable> stop(long time, ChronoUnit unit) {
        return s -> {
            s.makeBestPrice().cancelAfter(time, unit).take();
        };
    }
}