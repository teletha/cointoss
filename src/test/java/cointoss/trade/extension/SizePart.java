/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade.extension;

import java.math.BigDecimal;
import java.util.Set;

import cointoss.util.Num;

@SuppressWarnings("serial")
public class SizePart extends Num {

    public final double num;

    public final double half;

    public final Num halfN;

    private SizePart(double size) {
        super(BigDecimal.valueOf(size));

        this.num = size;
        this.half = size / 2;
        this.halfN = divide(2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[" + num + "]";
    }

    /**
     * Collect all values.
     * 
     * @return
     */
    static Set<SizePart> values() {
        return Set.of(new SizePart(2), new SizePart(0.2));
    }
}
