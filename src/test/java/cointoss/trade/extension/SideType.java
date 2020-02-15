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

import java.util.List;

import cointoss.Direction;
import cointoss.Directional;
import kiss.I;

public class SideType implements Directional {

    public final Direction side;

    public final int sign;

    /**
     * @param side
     */
    private SideType(Direction side) {
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
        return "[side:" + side + "]";
    }

    /**
     * Collect all values.
     * 
     * @return
     */
    static List<SideType> values() {
        return I.signal(Direction.values()).map(SideType::new).toList();
    }
}
