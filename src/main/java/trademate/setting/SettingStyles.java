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
import viewtify.dsl.Style;

/**
 * @version 2018/08/29 15:08:22
 */
public class SettingStyles extends StyleDSL {

    public static final Style Root = () -> {
        padding.vertical(20, px).horizontal(30, px);
    };

    public static final Style Block = () -> {
        padding.vertical(35, px);
        border.bottom.width(2, px).color(rgb(40, 40, 40));
    };

    public static final Style Heading = () -> {
        padding.bottom(5, px);
        font.size(16, px);
    };

    public static final Style Description = () -> {
        padding.top(2, px).bottom(7, px);
    };

    public static final Style FormRow = () -> {
        display.minHeight(30, px);
        padding.vertical(3, px);
        text.verticalAlign.middle();
    };

    public static final Style FormLabel = () -> {
        display.minWidth(100, px);
    };

    public static final Style FormInput = () -> {
        display.minWidth(160, px);
    };

    public static final Style FormInputMin = () -> {
        display.minWidth(60, px);
    };

    public static final Style CategoryPane = () -> {
        padding.top(40, px);
    };

    public static final Style CategoryLabel = () -> {
        display.width(200, px).height(20, px);
        padding.vertical(10, px).left(40, px);
        cursor.pointer();
        font.size(16, px);

        hover(() -> {
            background.color("derive(-fx-base, 15%)");
        });
    };

    public static final Style FormCheck = () -> {
        display.minWidth(60, px);
        text.align.center().verticalAlign.middle();
    };

    public static final Style FormCheck2 = () -> {
        display.minWidth(160, px);
        text.align.center().verticalAlign.middle();
    };
}
