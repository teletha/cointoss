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
 * @version 2018/07/31 9:47:26
 */
public enum SFD {

    /** The diff constants. */
    Plus5("1.05"),

    /** The diff constants. */
    Plus10("1.10"),

    /** The diff constants. */
    Plus15("1.15"),

    /** The diff constants. */
    Plus20("1.20"),

    /** The diff constants. */
    Minus5("0.95"),

    /** The diff constants. */
    Minus10("0.90"),

    /** The diff constants. */
    Minus15("0.85"),

    /** The diff constants. */
    Minus20("0.80");

    /** The latest price of BTC. */
    private static final Variable<Num> latestBTC = BitFlyer.BTC_JPY.executionsRealtimely()
            .startWith(BitFlyer.BTC_JPY.executionLatest())
            .map(Execution::price)
            .diff()
            .to();

    /** The human-readable percentage. */
    public final Num percentage;

    private final Num diff;

    /**
     * Create {@link SFD}.
     * 
     * @param diff
     */
    private SFD(String diff) {
        this.diff = Num.of(diff);
        this.percentage = this.diff.minus(Num.ONE).multiply(Num.HUNDRED);
    }

    /**
     * Calculate SFD boundary price.
     * 
     * @return
     */
    public Signal<Num> boundary() {
        return latestBTC.observe().map(this::calculate);
    }

    /**
     * Calculate sfd price.
     * 
     * @param price A current price.
     * @return A calculated SFD price.
     */
    final Num calculate(Num price) {
        if (diff.isGreaterThan(Num.ONE)) {
            return price.multiply(diff).scaleUp(0);
        } else {
            return price.multiply(diff).scaleDown(0);
        }
    }
}
