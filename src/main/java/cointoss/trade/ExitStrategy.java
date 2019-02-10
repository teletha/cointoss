/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
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
        }
    }
}
