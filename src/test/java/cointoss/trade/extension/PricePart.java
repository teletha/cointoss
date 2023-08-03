/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade.extension;

import cointoss.util.arithmetic.Num;

public class PricePart implements TradePart {

    public final double entry;

    public final Num entryN;

    public final double exit;

    public final Num exitN;

    public final double diff;

    public final Num diffN;

    public final double diffHalf;

    public final Num diffHalfN;

    public final double middle;

    public final Num middleN;

    public final double profit;

    public final Num profitN;

    public final double loss;

    public final Num lossN;

    /**
     * @param entry
     * @param exit
     */
    public PricePart(double entry, double exit) {
        this.entry = entry;
        this.entryN = Num.of(entry);
        this.exit = exit;
        this.exitN = Num.of(exit);
        this.diff = exit - entry;
        this.diffN = exitN.minus(entryN);
        this.diffHalf = diff / 2;
        this.diffHalfN = diffN.divide(2);
        this.middle = (exit + entry) / 2;
        this.middleN = exitN.plus(entryN).divide(2);

        this.profit = entry + diffHalf;
        this.profitN = entryN.plus(diffHalfN);
        this.loss = entry - diffHalf;
        this.lossN = entryN.minus(diffHalfN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[entry=" + entry + ", exit=" + exit + "]";
    }
}