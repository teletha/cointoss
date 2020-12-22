/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market;

import java.time.ZonedDateTime;

import cointoss.ticker.TimeseriesData;
import icy.manipulator.Icy;

@Icy
interface OpenInterestModel extends TimeseriesData {

    @Override
    @Icy.Property
    ZonedDateTime date();

    @Icy.Property
    double size();
}
