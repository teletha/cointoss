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
public interface PositionModel extends Directional {

    @Icy.Property
    @Override
    Direction direction();

    @Icy.Property
    Num price();

    @Icy.Property
    Num size();

    @Icy.Property
    ZonedDateTime date();

    @Icy.Property
    default Num profit() {
        return Num.ZERO;
    }
}
