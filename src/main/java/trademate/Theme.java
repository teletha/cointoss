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

import javafx.scene.paint.Color;

import cointoss.Directional;
import kiss.I;
import viewtify.model.Model;

public class Theme extends Model<Theme> {

    /** The current theme. */
    public static final Theme $ = I.make(Theme.class);

    /* Long position color. */
    public final Preference<Color> Long = initialize(Color.rgb(251, 189, 42));

    /* Short position color. */
    public final Preference<Color> Short = initialize(Color.rgb(247, 105, 77));

    /**
     * Hide Constructor.
     */
    private Theme() {
        restore().auto();
    }

    /**
     * Detec the color by direction.
     * 
     * @param direction
     * @return
     */
    public static Color colorBy(Directional direction) {
        Theme theme = I.make(Theme.class);
        return direction.isBuy() ? theme.Long.v : theme.Short.v;
    }
}