/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitflyer;

import org.junit.jupiter.api.Test;

import cointoss.util.arithmetic.Num;

/**
 * @version 2018/07/30 7:02:55
 */
class SFDTest {

    @Test
    void calculatePlus() {
        assert SFD.Plus5.calculate(Num.of(100)).is(Num.of(105));
        assert SFD.Plus5.calculate(Num.of(101)).is(Num.of(107));
        assert SFD.Plus5.calculate(Num.of(102)).is(Num.of(108));
        assert SFD.Plus5.calculate(Num.of(103)).is(Num.of(109));
        assert SFD.Plus5.calculate(Num.of(104)).is(Num.of(110));
        assert SFD.Plus5.calculate(Num.of(105)).is(Num.of(111));
        assert SFD.Plus5.calculate(Num.of(106)).is(Num.of(112));
        assert SFD.Plus5.calculate(Num.of(107)).is(Num.of(113));
        assert SFD.Plus5.calculate(Num.of(108)).is(Num.of(114));
        assert SFD.Plus5.calculate(Num.of(109)).is(Num.of(115));
        assert SFD.Plus5.calculate(Num.of(110)).is(Num.of(116));

        assert SFD.Plus10.calculate(Num.of(100)).is(Num.of(110));
        assert SFD.Plus10.calculate(Num.of(101)).is(Num.of(112));
        assert SFD.Plus10.calculate(Num.of(102)).is(Num.of(113));
        assert SFD.Plus10.calculate(Num.of(103)).is(Num.of(114));
        assert SFD.Plus10.calculate(Num.of(104)).is(Num.of(115));
        assert SFD.Plus10.calculate(Num.of(105)).is(Num.of(116));
        assert SFD.Plus10.calculate(Num.of(106)).is(Num.of(117));
        assert SFD.Plus10.calculate(Num.of(107)).is(Num.of(118));
        assert SFD.Plus10.calculate(Num.of(108)).is(Num.of(119));
        assert SFD.Plus10.calculate(Num.of(109)).is(Num.of(120));
        assert SFD.Plus10.calculate(Num.of(110)).is(Num.of(121));
    }

    @Test
    void calculateMinus() {
        assert SFD.Minus5.calculate(Num.of(100)).is(Num.of(95));
        assert SFD.Minus5.calculate(Num.of(101)).is(Num.of(95));
        assert SFD.Minus5.calculate(Num.of(102)).is(Num.of(96));
        assert SFD.Minus5.calculate(Num.of(103)).is(Num.of(97));
        assert SFD.Minus5.calculate(Num.of(104)).is(Num.of(98));
        assert SFD.Minus5.calculate(Num.of(105)).is(Num.of(99));
        assert SFD.Minus5.calculate(Num.of(106)).is(Num.of(100));
        assert SFD.Minus5.calculate(Num.of(107)).is(Num.of(101));
        assert SFD.Minus5.calculate(Num.of(108)).is(Num.of(102));
        assert SFD.Minus5.calculate(Num.of(109)).is(Num.of(103));
        assert SFD.Minus5.calculate(Num.of(110)).is(Num.of(104));

        assert SFD.Minus10.calculate(Num.of(100)).is(Num.of(90));
        assert SFD.Minus10.calculate(Num.of(101)).is(Num.of(90));
        assert SFD.Minus10.calculate(Num.of(102)).is(Num.of(91));
        assert SFD.Minus10.calculate(Num.of(103)).is(Num.of(92));
        assert SFD.Minus10.calculate(Num.of(104)).is(Num.of(93));
        assert SFD.Minus10.calculate(Num.of(105)).is(Num.of(94));
        assert SFD.Minus10.calculate(Num.of(106)).is(Num.of(95));
        assert SFD.Minus10.calculate(Num.of(107)).is(Num.of(96));
        assert SFD.Minus10.calculate(Num.of(108)).is(Num.of(97));
        assert SFD.Minus10.calculate(Num.of(109)).is(Num.of(98));
        assert SFD.Minus10.calculate(Num.of(110)).is(Num.of(99));
    }
}