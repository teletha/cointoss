/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.execution;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import cointoss.Direction;
import cointoss.ticker.Span;
import cointoss.util.Chrono;
import hypatia.Num;

public class Executions {

    /**
     * Create the specified numbers of {@link Execution}.
     * 
     * @param numbers
     * @return
     */
    public static List<Execution> random(int numbers) {
        List<Execution> list = new ArrayList();

        for (int i = 0; i < numbers; i++) {
            list.add(Execution.with.direction(Direction.random(), Num.random(1, 10)).price(Num.random(1, 10)));
        }

        return list;
    }

    /**
     * Create the specified date-ranged {@link Execution}s.
     * 
     * @return
     */
    public static List<Execution> random(ZonedDateTime start, ZonedDateTime end, Span span) {
        List<Execution> list = new ArrayList();
        ZonedDateTime current = start;
        while (current.isBefore(end) || current.isEqual(end)) {
            list.add(Execution.with.direction(Direction.random(), Num.random(1, 10)).price(Num.random(1, 10)).date(current));
            current = current.plus(span.duration);
        }
        return list;
    }

    /**
     * Create the specified numbers of {@link Execution}.
     * 
     * @param numbers
     * @return
     */
    public static List<Execution> random(int numbers, Duration interval) {
        List<Execution> list = new ArrayList();
        ZonedDateTime date = Chrono.MIN;

        for (int i = 0; i < numbers; i++) {
            list.add(Execution.with.direction(Direction.random(), Num.random(1, 10)).price(Num.random(1, 10)).date(date));
            date = date.plus(interval);
        }

        return list;
    }

    /**
     * Create the sequence of {@link Execution}s.
     * 
     * @param size
     * @param price
     * @return
     */
    public static List<Execution> sequence(int count, Direction side, double size, double price) {
        List<Execution> list = new ArrayList();

        for (int i = 0; i < count; i++) {
            list.add(Execution.with.direction(side, size)
                    .price(price)
                    .consecutive(i == 0 ? ExecutionModel.ConsecutiveDifference
                            : side.isPositive() ? ExecutionModel.ConsecutiveSameBuyer : ExecutionModel.ConsecutiveSameSeller));
        }
        return list;
    }

}