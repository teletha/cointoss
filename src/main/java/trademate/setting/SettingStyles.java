/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.setting;

import static stylist.StyleDSL.*;

import stylist.Style;
import stylist.StyleDeclarable;
import stylist.value.Numeric;

public interface SettingStyles extends StyleDeclarable {

    Numeric blockSpace = new Numeric(35, px);

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
}