/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.setting;

import stylist.Style;
import stylist.StyleDSL;
import stylist.value.Numeric;

/**
 * @version 2018/09/06 23:46:11
 */
public interface SettingStyles extends StyleDSL {

    Numeric blockSpace = new Numeric(35, px);

    Style Root = () -> {
        padding.vertical(20, px).horizontal(30, px);
    };

    Style Block = () -> {
        padding.bottom(blockSpace);
        border.bottom.width(2, px).color($.rgb(40, 40, 40));
    };

    Style Heading = () -> {
        padding.top(blockSpace).bottom(5, px);
        font.size(16, px);
    };

    Style Description = () -> {
        padding.top(2, px).bottom(7, px);
    };

    Style Warning = () -> {
        font.color($.rgb(220, 100, 100));
    };

    Style FormRow = () -> {
        display.minHeight(30, px);
        padding.vertical(3, px);
        text.verticalAlign.middle();
    };

    Style FormLabel = () -> {
        display.minWidth(120, px);
        padding.top(4, px);
    };

    Style FormHeaderLabel = () -> {
        text.align.center();
    };

    Style FormInput = () -> {
        display.minWidth(170, px);
        margin.right(10, px);
    };

    Style FormInputMin = () -> {
        display.minWidth(60, px).width(60, px);
    };

    Style FormCheck = () -> {
        display.minWidth(60, px);
        text.align.center().verticalAlign.middle();
    };

    Style FormCheck2 = () -> {
        display.minWidth(160, px);
        text.align.center().verticalAlign.middle();
    };
}
