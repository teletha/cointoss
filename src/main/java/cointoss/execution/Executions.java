/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.execution;

import java.time.LocalDateTime;

import cointoss.Direction;
import cointoss.util.Chrono;
import cointoss.util.Num;

public class Executions {

    /** The empty object. */
    public static final Execution BASE = Execution.with.buy(Num.ZERO).date(Chrono.utc(2000, 1, 1));

    /**
     * Create {@link ExecutionModel} with the specified values.
     * 
     * @param values A list of values.
     */
    public static Execution of(String... values) {
        Num size = Num.of(values[4]);

        return Execution.with.direction(Direction.parse(values[2]), size)
                .cumulativeSize(size)
                .price(Num.of(values[3]))
                .id(Long.parseLong(values[0]))
                .date(LocalDateTime.parse(values[1]).atZone(Chrono.UTC))
                .consecutive(Integer.parseInt(values[5]))
                .delay(Integer.parseInt(values[6]));
    }
}