/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import org.junit.Test;

import cointoss.Amount;
import cointoss.Side;

/**
 * @version 2017/08/21 21:28:04
 */
public class AmountTest {

    @Test
    public void plusDirection() throws Exception {
        Amount amount = Amount.of(10);
        assert amount.plus(Side.BUY, 1).is(11);
        assert amount.plus(Side.SELL, 1).is(9);
    }

    @Test
    public void minusDirection() throws Exception {
        Amount amount = Amount.of(10);
        assert amount.minus(Side.BUY, 1).is(9);
        assert amount.minus(Side.SELL, 1).is(11);
    }

    @Test
    public void multiplyDirection() throws Exception {
        Amount amount = Amount.of(100);
        assert amount.ratio(Side.BUY, 1).is(101);
        assert amount.ratio(Side.SELL, 1).is(99);
    }
}
