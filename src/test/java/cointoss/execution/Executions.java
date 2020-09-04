/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.execution;

import java.util.ArrayList;
import java.util.List;

import cointoss.Direction;
import cointoss.util.arithmeric.Num;

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
                    .consecutive(i == 0 ? ExecutionModel.ConsecutiveDifference : side.isBuy() ? ExecutionModel.ConsecutiveSameBuyer : ExecutionModel.ConsecutiveSameSeller));
        }
        return list;
    }

}