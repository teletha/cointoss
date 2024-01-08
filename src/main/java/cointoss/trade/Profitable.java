/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade;

import cointoss.util.arithmetic.Num;

public interface Profitable {

    /**
     * Calculate total profit or loss on the current price.
     * 
     * @param currentPrice A current price.
     * @return A total profit or loss of this entry.
     */
    default Num profit(Num currentPrice) {
        return realizedProfit().plus(unrealizedProfit(currentPrice));
    }

    /**
     * A realized profit or loss of this entry.
     * 
     * @return A realized profit or loss of this entry.
     */
    Num realizedProfit();

    /**
     * Calculate unrealized profit or loss on the current price.
     * 
     * @param currentPrice A current price.
     * @return An unrealized profit or loss of this entry.
     */
    Num unrealizedProfit(Num currentPrice);

    /**
     * The total commission.
     * 
     * @return The total commission.
     */
    Num commission();
}