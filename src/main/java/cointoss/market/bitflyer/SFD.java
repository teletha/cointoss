/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitflyer;

import java.math.RoundingMode;

import cointoss.util.Num;
import kiss.I;
import kiss.Signal;
import kiss.Ⅲ;

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

    /** The latest SFD stream. */
    private static Signal<Ⅲ<Num, Num, Num>> latest;

    /** The human-readable percentage. */
    public final Num percentage;

    /** The internal percentage. */
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
        return now().map(v -> calculate(v.ⅱ));
    }

    /**
     * Calculate sfd price.
     * 
     * @param price A current price.
     * @return A calculated SFD price.
     */
    final Num calculate(Num price) {
        if (diff.isGreaterThan(Num.ONE)) {
            return price.multiply(diff).scale(0, RoundingMode.UP);
        } else {
            return price.multiply(diff).scale(0, RoundingMode.DOWN);
        }
    }

    /**
     * Calculate the current difference percentage.
     * 
     * @return
     */
    public static synchronized Signal<Ⅲ<Num, Num, Num>> now() {
        if (latest == null) {
            latest = BitFlyer.FX_BTC_JPY.executionsRealtimely()
                    .diff()
                    .combineLatest(BitFlyer.BTC_JPY.executionsRealtimely().startWith(BitFlyer.BTC_JPY.executionLatest().diff()))
                    .map(e -> {
                        Num fx = e.ⅰ.price;
                        Num btc = e.ⅱ.price;
                        Num diff;

                        if (btc.isLessThanOrEqual(fx)) {
                            diff = fx.divide(btc).minus(Num.ONE).multiply(Num.HUNDRED);
                        } else {
                            diff = btc.divide(fx).minus(Num.ONE).multiply(Num.HUNDRED).negate();
                        }
                        return I.pair(fx, btc, diff);
                    })
                    .retryWhen(BitFlyer.FX_BTC_JPY.retryPolicy(200))
                    .share();
        }
        return latest;
    }
}
