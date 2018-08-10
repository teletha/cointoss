/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade;

import cointoss.Market;
import cointoss.util.Num;
import kiss.Variable;

/**
 * @version 2018/08/10 7:53:31
 */
public class ExitStrategy {

    public void execute(Market market) {
        Variable<Num> size = market.positions.size;
        
        if (size.isNot(Num.ZERO)) {
            market.orderBook.computeBestPrice(market.positions, size.v, market.service.minimumTargetSize())
        }
    }
}
