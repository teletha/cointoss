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

import cointoss.Execution;
import cointoss.util.Num;
import kiss.Signal;
import kiss.Variable;

/**
 * @version 2018/07/30 6:46:51
 */
public class SFD {

    /** The diff constants. */
    static final Num plus5 = Num.of("1.05");

    /** The diff constants. */
    static final Num plus10 = Num.of("1.10");

    /** The diff constants. */
    static final Num plus15 = Num.of("1.15");

    /** The diff constants. */
    static final Num plus20 = Num.of("1.20");

    /** The diff constants. */
    static final Num minus5 = Num.of("0.95");

    /** The diff constants. */
    static final Num minus10 = Num.of("0.90");

    /** The diff constants. */
    static final Num minus15 = Num.of("0.85");

    /** The diff constants. */
    static final Num minus20 = Num.of("0.80");

    /** The latest price of BTC. */
    private final Variable<Num> latestBTC = BitFlyer.BTC_JPY.executionsRealtimely().map(Execution::price).diff().to();

    /**
     * Calculate SFD boundary price.
     * 
     * @return
     */
    public Signal<Num> calculatePlus5() {
        return latestBTC.observe().map(price -> calculate(price, plus5));
    }

    /**
     * Calculate sfd price.
     * 
     * @param price A current price.
     * @param diff The percentage.
     * @return A calculated SFD price.
     */
    static final Num calculate(Num price, Num diff) {
        if (diff.isGreaterThan(Num.ONE)) {
            return price.multiply(diff).scaleUp(0);
        } else {
            return price.multiply(diff).scaleDown(0);
        }
    }
}
