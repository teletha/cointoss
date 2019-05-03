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
    public static final Execution BASE = Execution.with();

    static {
        // don't modify these initial values
        BASE.date(Chrono.utc(2000, 1, 1));
        BASE.side = Direction.BUY;
        BASE.price = Num.ZERO;
        BASE.size = Num.ZERO;
    }

    /**
     * Create {@link ExecutionModel} with the specified values.
     * 
     * @param values A list of values.
     */
    public static Execution of(String... values) {
        Execution e = Execution.with();
        e.id = Long.parseLong(values[0]);
        e.date(LocalDateTime.parse(values[1]).atZone(Chrono.UTC));
        e.side = Direction.parse(values[2]);
        e.price = Num.of(values[3]);
        e.size = e.cumulativeSize = Num.of(values[4]);
        e.consecutive = Integer.parseInt(values[5]);
        e.delay = Integer.parseInt(values[6]);

        return e;
    }
}
