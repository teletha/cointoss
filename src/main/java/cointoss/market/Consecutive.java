/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market;

import java.time.ZonedDateTime;

import cointoss.Direction;
import cointoss.execution.Execution;

/**
 * 
 */
public class Consecutive {

    /** The previous data. */
    private Direction side;

    /** The previous data. */
    private ZonedDateTime date;

    public int compute(Direction side, ZonedDateTime date) {
        int consecutive;

        if (date.equals(this.date)) {
            if (side != this.side) {
                consecutive = Execution.ConsecutiveDifference;
            } else if (side == Direction.BUY) {
                consecutive = Execution.ConsecutiveSameBuyer;
            } else {
                consecutive = Execution.ConsecutiveSameSeller;
            }
        } else {
            consecutive = Execution.ConsecutiveDifference;
        }

        this.side = side;
        this.date = date;

        return consecutive;
    }
}
