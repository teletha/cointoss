/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart;

import javafx.scene.paint.Color;

import kiss.Variable;

public record LineStyle(Color color, double width, double[] dash) {

    public LineStyle(Variable<Color> color) {
        this(color.v);
    }

    public LineStyle(Color color) {
        this(color, 0.6);
    }

    public LineStyle(Color color, double width) {
        this(color, width, null);
    }
}