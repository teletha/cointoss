/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import cointoss.Direction;
import stylist.Style;
import stylist.StyleDSL;
import stylist.ValueStyle;
import stylist.value.Color;

public interface TradeMateStyle extends StyleDSL {

    Color BUY = Color.rgb(251, 189, 42);

    Color SELL = Color.rgb(247, 105, 77);

    Style Long = () -> {
        font.color(BUY);
    };

    Style Short = () -> {
        font.color(SELL);
    };

    ValueStyle<Direction> Side = side -> {
        font.color(side.isBuy() ? BUY : SELL);
    };
}