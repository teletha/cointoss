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

import java.math.BigDecimal;

import cointoss.util.arithmetic.Num;

@SuppressWarnings("serial")
public class SizePart extends Num implements TradePart {

    public final double num;

    public final double half;

    public final Num halfN;

    public SizePart(double size) {
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
}