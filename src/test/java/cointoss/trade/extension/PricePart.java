/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade.extension;

import java.util.Set;

import cointoss.util.Num;

public class PricePart {

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
    private PricePart(double entry, double exit) {
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

    /**
     * Collect all values.
     * 
     * @return
     */
    static Set<PricePart> values() {
        return Set.of(new PricePart(10, 20), new PricePart(20, 10), new PricePart(0.1, 0.2), new PricePart(0.2, 0.1));
    }
}
