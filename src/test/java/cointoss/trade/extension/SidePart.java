/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade.extension;

import cointoss.Direction;
import hypatia.Orientational;

public class SidePart implements Orientational<Direction>, TradePart {

    /** The actual side. */
    public final Direction side;

    /** The calculation sign. */
    public final int sign;

    /**
     * @param side
     */
    public SidePart(Direction side) {
        this.side = side;
        this.sign = side.isPositive() ? 1 : -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPositive() {
        return side.isPositive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Direction orientation() {
        return side;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[" + side + "]";
    }
}