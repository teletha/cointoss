/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.setting;

import static stylist.StyleDSL.$;
import static stylist.StyleDSL.font;
import static stylist.StyleDSL.padding;
import static stylist.StyleDSL.px;

import stylist.Style;
import stylist.StyleDeclarable;
import stylist.value.Numeric;

public interface SettingStyles extends StyleDeclarable {

    Numeric blockSpace = new Numeric(25, px);

    Style Heading = () -> {
        padding.top(blockSpace).bottom(10, px);
        font.size(14, px);
    };

    Style Description = () -> {
        padding.bottom(5, px);
    };

    Style Warning = () -> {
        font.color($.rgb(220, 100, 100));
    };
}