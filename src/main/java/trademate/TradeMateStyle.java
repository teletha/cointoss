/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate;

import cointoss.Side;
import stylist.Style;
import stylist.StyleDSL;
import stylist.ValueStyle;
import stylist.value.Color;

/**
 * @version 2018/09/07 10:57:21
 */
public interface TradeMateStyle extends StyleDSL {

    Color BUY = Color.rgb(251, 189, 42);

    Color SELL = Color.rgb(247, 105, 77);

    Style Long = () -> {
        font.color(BUY);
    };

    Style Short = () -> {
        font.color(SELL);
    };

    ValueStyle<Side> Side = side -> {
        font.color(side.isBuy() ? BUY : SELL);
    };
}
