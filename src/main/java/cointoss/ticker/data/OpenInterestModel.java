/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker.data;

import java.time.ZonedDateTime;

import cointoss.util.feather.TemporalData;
import icy.manipulator.Icy;

@Icy
interface OpenInterestModel extends TemporalData {

    @Override
    @Icy.Property
    ZonedDateTime date();

    @Icy.Property
    double size();
}