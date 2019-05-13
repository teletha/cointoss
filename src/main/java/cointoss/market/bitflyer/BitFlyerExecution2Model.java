/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import cointoss.Direction;
import cointoss.execution.Execution;
import icy.manipulator.Icy;

@Icy(grouping = 2)
public class BitFlyerExecution2Model extends Execution {

    static final BitFlyerExecution2 NONE = BitFlyerExecution2.with.direction(Direction.BUY, 0);

    /** Buyer id of this execution. */
    String buyer = "";

    /** Seller id of this execution. */
    String seller = "";

    /**
     * Estimate delay and consecutive type.
     * 
     * @param previous
     * @return
     */
    BitFlyerExecution2Model estimate(BitFlyerExecution2 previous) {
        return this;
    }

}
