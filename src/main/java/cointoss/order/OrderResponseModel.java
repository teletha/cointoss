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

import cointoss.util.arithmetic.Num;
import icy.manipulator.Icy;

@Icy
public interface OrderResponseModel {

    @Icy.Property
    OrderResponseType type();

    @Icy.Property
    Num size();

    @Icy.Property
    Num price();
}
