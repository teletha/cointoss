/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import static cointoss.util.Num.*;

import org.junit.Test;

/**
 * @version 2017/09/24 12:22:52
 */
public class NumTest {

    @Test
    public void max() throws Exception {
        assert Num.max(ONE).is(1);
        assert Num.max(ONE, TWO, TEN).is(10);
        assert Num.max(ONE, TWO, of(-10)).is(2);
        assert Num.max().isNaN();
        assert Num.max((Num[]) null).isNaN();
        assert Num.max(ONE, null, TWO).is(2);
        assert Num.max(null, null, null).isNaN();
    }

    @Test
    public void min() throws Exception {
        assert Num.min(ONE).is(1);
        assert Num.min(ONE, TWO, TEN).is(1);
        assert Num.min(ONE, TWO, of(-10)).is(-10);
        assert Num.min().isNaN();
        assert Num.min((Num[]) null).isNaN();
        assert Num.min(ONE, null, TWO).is(1);
        assert Num.min(null, null, null).isNaN();
    }

    @Test
    public void plus() throws Exception {
        assert ONE.plus(TWO).is(3);
        assert ONE.plus(-1).is(0);
        assert ONE.plus("5.5").is(6.5);
        assert ONE.plus((Num) null).isNaN();
    }

    @Test
    public void minus() throws Exception {
        assert ONE.minus(TWO).is(-1);
        assert ONE.minus(-1).is(2);
        assert ONE.minus("5.5").is(-4.5);
        assert ONE.minus((Num) null).isNaN();
    }

    @Test
    public void multiply() throws Exception {
        assert ONE.multiply(TWO).is(2);
        assert ONE.multiply(-1).is(-1);
        assert ONE.multiply("5.5").is(5.5);
        assert ONE.multiply((Num) null).isNaN();
    }

    @Test
    public void divide() throws Exception {
        assert ONE.divide(TWO).is(0.5);
        assert ONE.divide(-1).is(-1);
        assert ONE.divide("0.5").is(2);
        assert ONE.divide((Num) null).isNaN();
    }
}
