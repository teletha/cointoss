/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker.data;

import java.time.ZonedDateTime;

import cointoss.Direction;
import cointoss.util.feather.Timelinable;
import hypatia.Num;
import hypatia.Orientational;
import icy.manipulator.Icy;

@Icy
interface LiquidationModel extends Timelinable, Orientational<Direction> {

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean isPositive() {
        return orientation().isPositive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Icy.Property
    ZonedDateTime date();

    @Override
    @Icy.Property
    Direction orientation();

    @Icy.Property
    double size();

    @Icy.Property
    Num price();
}