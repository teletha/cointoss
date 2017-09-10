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

import eu.verdelhan.ta4j.Decimal;

/**
 * @version 2017/09/10 20:33:40
 */
public class DecimalTest {

    @Test
    public void minusDirectional() throws Exception {
        Decimal amount = Decimal.valueOf(100);
        assert amount.minus(Side.BUY, 20).is(80);
        assert amount.minus(Side.SELL, 20).is(120);
    }

    @Test
    public void lessThanDirectional() throws Exception {
        Decimal amount = Decimal.valueOf(100);
        assert amount.isLessThan(Side.BUY, 120) == true;
        assert amount.isLessThan(Side.BUY, 80) == false;
        assert amount.isLessThan(Side.SELL, 120) == false;
        assert amount.isLessThan(Side.SELL, 80) == true;
    }
}
