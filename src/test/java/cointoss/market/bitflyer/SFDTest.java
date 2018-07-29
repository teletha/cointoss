/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import org.junit.jupiter.api.Test;

import cointoss.util.Num;

/**
 * @version 2018/07/30 7:02:55
 */
class SFDTest {

    @Test
    void calculatePlus() {
        assert SFD.calculate(Num.of(100), SFD.plus5).is(Num.of(105));
        assert SFD.calculate(Num.of(101), SFD.plus5).is(Num.of(107));
        assert SFD.calculate(Num.of(102), SFD.plus5).is(Num.of(108));
        assert SFD.calculate(Num.of(103), SFD.plus5).is(Num.of(109));
        assert SFD.calculate(Num.of(104), SFD.plus5).is(Num.of(110));
        assert SFD.calculate(Num.of(105), SFD.plus5).is(Num.of(111));
        assert SFD.calculate(Num.of(106), SFD.plus5).is(Num.of(112));
        assert SFD.calculate(Num.of(107), SFD.plus5).is(Num.of(113));
        assert SFD.calculate(Num.of(108), SFD.plus5).is(Num.of(114));
        assert SFD.calculate(Num.of(109), SFD.plus5).is(Num.of(115));
        assert SFD.calculate(Num.of(110), SFD.plus5).is(Num.of(116));

        assert SFD.calculate(Num.of(100), SFD.plus10).is(Num.of(110));
        assert SFD.calculate(Num.of(101), SFD.plus10).is(Num.of(112));
        assert SFD.calculate(Num.of(102), SFD.plus10).is(Num.of(113));
        assert SFD.calculate(Num.of(103), SFD.plus10).is(Num.of(114));
        assert SFD.calculate(Num.of(104), SFD.plus10).is(Num.of(115));
        assert SFD.calculate(Num.of(105), SFD.plus10).is(Num.of(116));
        assert SFD.calculate(Num.of(106), SFD.plus10).is(Num.of(117));
        assert SFD.calculate(Num.of(107), SFD.plus10).is(Num.of(118));
        assert SFD.calculate(Num.of(108), SFD.plus10).is(Num.of(119));
        assert SFD.calculate(Num.of(109), SFD.plus10).is(Num.of(120));
        assert SFD.calculate(Num.of(110), SFD.plus10).is(Num.of(121));
    }

    @Test
    void calculateMinus() {
        assert SFD.calculate(Num.of(100), SFD.minus5).is(Num.of(95));
        assert SFD.calculate(Num.of(101), SFD.minus5).is(Num.of(95));
        assert SFD.calculate(Num.of(102), SFD.minus5).is(Num.of(96));
        assert SFD.calculate(Num.of(103), SFD.minus5).is(Num.of(97));
        assert SFD.calculate(Num.of(104), SFD.minus5).is(Num.of(98));
        assert SFD.calculate(Num.of(105), SFD.minus5).is(Num.of(99));
        assert SFD.calculate(Num.of(106), SFD.minus5).is(Num.of(100));
        assert SFD.calculate(Num.of(107), SFD.minus5).is(Num.of(101));
        assert SFD.calculate(Num.of(108), SFD.minus5).is(Num.of(102));
        assert SFD.calculate(Num.of(109), SFD.minus5).is(Num.of(103));
        assert SFD.calculate(Num.of(110), SFD.minus5).is(Num.of(104));

        assert SFD.calculate(Num.of(100), SFD.minus10).is(Num.of(90));
        assert SFD.calculate(Num.of(101), SFD.minus10).is(Num.of(90));
        assert SFD.calculate(Num.of(102), SFD.minus10).is(Num.of(91));
        assert SFD.calculate(Num.of(103), SFD.minus10).is(Num.of(92));
        assert SFD.calculate(Num.of(104), SFD.minus10).is(Num.of(93));
        assert SFD.calculate(Num.of(105), SFD.minus10).is(Num.of(94));
        assert SFD.calculate(Num.of(106), SFD.minus10).is(Num.of(95));
        assert SFD.calculate(Num.of(107), SFD.minus10).is(Num.of(96));
        assert SFD.calculate(Num.of(108), SFD.minus10).is(Num.of(97));
        assert SFD.calculate(Num.of(109), SFD.minus10).is(Num.of(98));
        assert SFD.calculate(Num.of(110), SFD.minus10).is(Num.of(99));
    }
}
