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

import java.util.Set;

import cointoss.Direction;
import cointoss.Directional;

public class SidePart implements Directional, TradePart {

    /** The actual side. */
    public final Direction side;

    /** The calculation sign. */
    public final int sign;

    /**
     * @param side
     */
    public SidePart(Direction side) {
        this.side = side;
        this.sign = side.isBuy() ? 1 : -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Direction direction() {
        return side;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[" + side + "]";
    }

    /**
     * Collect all values.
     * 
     * @return
     */
    static Set<SidePart> values() {
        return Set.of(new SidePart(Direction.BUY), new SidePart(Direction.SELL));
    }
}
