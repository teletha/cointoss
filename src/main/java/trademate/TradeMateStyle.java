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
import viewtify.util.FXUtils;

public interface TradeMateStyle extends StyleDSL {

    Color BUY = Color.rgb(251, 189, 42);

    javafx.scene.paint.Color BUY_FX = FXUtils.color(BUY);

    Color SELL = Color.rgb(247, 105, 77);

    javafx.scene.paint.Color SELL_FX = FXUtils.color(SELL);

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