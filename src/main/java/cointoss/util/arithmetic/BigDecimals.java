/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.arithmetic;

import java.math.BigDecimal;

class BigDecimals {

    /**
     * Helper method to calculate round-up value on {@link BigDecimal} context.
     * 
     * @param value A target value.
     * @param base A base value.
     * @return A round-up value.
     */
    static BigDecimal ceiling(BigDecimal value, BigDecimal base) {
        BigDecimal rem = value.remainder(base);
        return rem.signum() == 0 ? value : value.subtract(rem).add(base);
    }

    /**
     * Helper method to calculate round-down value on {@link BigDecimal} context.
     * 
     * @param value A target value.
     * @param base A base value.
     * @return A round-down value.
     */
    static BigDecimal floor(BigDecimal value, BigDecimal base) {
        BigDecimal rem = value.remainder(base);
        return rem.signum() == 0 ? value : value.subtract(rem);
    }
}
