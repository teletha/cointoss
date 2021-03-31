/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import static javafx.scene.paint.Color.*;

import java.util.List;

import cointoss.Directional;
import cointoss.util.arithmetic.Num;
import javafx.scene.paint.Color;
import kiss.I;
import viewtify.model.Model;

public class Theme extends Model<Theme> {

    /** The current theme. */
    public static final Theme $ = I.make(Theme.class);

    /** Built-in Theme */
    public static final Theme Binance = new Theme("Binance").buy.with(rgb(2, 192, 118)).sell.with(rgb(248, 73, 96));

    /** Built-in Theme */
    public static final Theme Bitflyer = new Theme("Bitflyer").buy.with(rgb(251, 189, 42)).sell.with(rgb(247, 105, 77));

    /** Built-in Theme */
    public static final Theme ByBit = new Theme("Bybit").buy.with(rgb(121, 184, 61)).sell.with(rgb(201, 56, 96));

    /** Built-in Theme */
    public static final Theme KuromaChart = new Theme("Kuroma Chart").buy.with(rgb(62, 179, 112)).sell.with(rgb(233, 84, 107));

    /* Long position color. */
    public final Preference<Color> buy = initialize(rgb(251, 189, 42));

    /* Short position color. */
    public final Preference<Color> sell = initialize(rgb(247, 105, 77));

    /** The name of this {@link Theme}. */
    public final String name;

    /**
     * Hide Constructor.
     */
    private Theme() {
        this("Custom");
    }

    /**
     * Hide Constructor.
     */
    private Theme(String name) {
        this.name = name;
        if (name.equals("Custom")) restore().auto();
    }

    /**
     * Apply this {@link Theme}.
     */
    public void apply() {
        if ($ != this) {
            $.buy.set(buy);
            $.sell.set(sell);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Detec the color by direction.
     * 
     * @param direction
     * @return
     */
    public static Color colorBy(Directional direction) {
        Theme theme = I.make(Theme.class);
        return direction.isBuy() ? theme.buy.v : theme.sell.v;
    }

    /**
     * Detec the color by number.
     * 
     * @return
     */
    public static Color colorBy(Num num) {
        Theme theme = I.make(Theme.class);
        return num.isPositiveOrZero() ? theme.buy.v : theme.sell.v;
    }

    /**
     * List up all built-in {@link Theme}s.
     * 
     * @return
     */
    public static List<Theme> builtins() {
        return I.signal(Theme.class.getFields())
                .take(field -> field.getType() == Theme.class)
                .map(field -> field.get(null))
                .as(Theme.class)
                .toList();
    }
}