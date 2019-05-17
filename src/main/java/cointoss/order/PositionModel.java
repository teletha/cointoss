/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.order;

import java.time.ZonedDateTime;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.util.Num;
import icy.manipulator.Icy;

@Icy(setterModifier = "final")
public abstract class PositionModel implements Directional {

    /**
     * {@inheritDoc}
     */
    @Icy.Property
    @Override
    public abstract Direction direction();

    @Icy.Property
    public abstract Num price();

    @Icy.Property
    public abstract Num size();

    @Icy.Property
    public Num profit() {
        return Num.ZERO;
    }

    @Icy.Property
    public abstract ZonedDateTime date();
}
