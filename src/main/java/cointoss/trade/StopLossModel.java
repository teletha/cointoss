/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade;

import cointoss.util.Num;
import icy.manipulator.Icy;

@Icy
public abstract class StopLossModel {

    @Icy.Property
    public abstract Num price();

    @Icy.Overload("price")
    private Num price(long size) {
        return Num.of(size);
    }

    @Icy.Overload("price")
    private Num price(double size) {
        return Num.of(size);
    }
}
