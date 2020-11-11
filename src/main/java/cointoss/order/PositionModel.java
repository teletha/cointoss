/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import java.time.ZonedDateTime;

import org.apache.logging.log4j.util.PerformanceSensitive;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.util.arithmetic.Num;
import icy.manipulator.Icy;

@Icy
abstract class PositionModel implements Directional {

    @Icy.Property
    @Override
    public abstract Direction direction();

    @Icy.Property(mutable = true)
    public abstract Num price();

    @Icy.Property(mutable = true)
    public abstract Num size();

    @Icy.Property
    public abstract ZonedDateTime date();

    /**
     * Calculate total profit or loss on the current price.
     * 
     * @param currentPrice A current price.
     * @return A total profit or loss of this entry.
     */
    @PerformanceSensitive
    public final Num profit(Num currentPrice) {
        return currentPrice.minus(this, price()).multiply(size());
    }
}