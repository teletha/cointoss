/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.setting;

import stylist.StyleDSL;
import stylist.value.Numeric;
import viewtify.dsl.Style;

/**
 * @version 2018/08/29 15:08:22
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
        font.color($.rgb(150, 50, 50));
    };

    Style FormRow = () -> {
        display.minHeight(30, px);
        padding.vertical(3, px);
        text.verticalAlign.middle();
    };

    Style FormLabel = () -> {
        display.minWidth(100, px);
    };

    Style FormInput = () -> {
        display.minWidth(160, px);
    };

    Style FormInputMin = () -> {
        display.minWidth(60, px);
    };

    Style CategoryPane = () -> {
        padding.top(40, px);
    };

    Style CategoryLabel = () -> {
        display.width(200, px).height(20, px);
        padding.vertical(10, px).left(40, px);
        cursor.pointer();
        font.size(16, px);

        $.hover(() -> {
            background.color("derive(-fx-base, 15%)");
        });
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
