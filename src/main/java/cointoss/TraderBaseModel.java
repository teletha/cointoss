/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import cointoss.util.Num;
import cointoss.util.ObservableNumProperty;
import icy.manipulator.Icy;

@Icy
interface TraderBaseModel {

    /**
     * Return the current hold size of target currency. Positive number means long position,
     * negative number means short position. Zero means no position.
     * 
     * @return A current hold size.
     */
    @Icy.Property(setterModifier = "final", custom = ObservableNumProperty.class)
    default Num holdSize() {
        return Num.ZERO;
    }

    /**
     * Return the maximum hold size of target currency. (historical data)
     * 
     * @return A maximum hold size.
     */
    @Icy.Property(setterModifier = "final", custom = ObservableNumProperty.class)
    default Num holdMaxSize() {
        return Num.ZERO;
    }
}
