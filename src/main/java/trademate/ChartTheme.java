/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import static javafx.scene.paint.Color.rgb;

import java.util.List;

import javafx.scene.paint.Color;

import cointoss.Directional;
import cointoss.util.arithmetic.Num;
import kiss.I;
import viewtify.model.PreferenceModel;

public class ChartTheme extends PreferenceModel<ChartTheme> {

    /** The current theme. */
    public static final ChartTheme $ = I.make(ChartTheme.class);

    /** Built-in Theme */
    public static final ChartTheme Binance = new ChartTheme("Binance").buy.with(rgb(2, 192, 118)).sell.with(rgb(248, 73, 96));

    /** Built-in Theme */
    public static final ChartTheme Bitflyer = new ChartTheme("Bitflyer").buy.with(rgb(251, 189, 42)).sell.with(rgb(247, 105, 77));

    /** Built-in Theme */
    public static final ChartTheme ByBit = new ChartTheme("Bybit").buy.with(rgb(121, 184, 61)).sell.with(rgb(201, 56, 96));

    /** Built-in Theme */
    public static final ChartTheme FTX = new ChartTheme("FTX").buy.with(rgb(38, 166, 154)).sell.with(rgb(239, 83, 80));

    /** Built-in Theme */
    public static final ChartTheme Mono = new ChartTheme("Mono").buy.with(rgb(110, 110, 110)).sell.with(rgb(230, 230, 230));

    /* Long position color. */
    public final Preference<Color> buy = initialize(rgb(251, 189, 42));

    /* Short position color. */
    public final Preference<Color> sell = initialize(rgb(247, 105, 77));

    /** The name of this {@link ChartTheme}. */
    public final String name;

    /**
     * Hide Constructor.
     */
    private ChartTheme() {
        this("Custom");
    }

    /**
     * Hide Constructor.
     */
    private ChartTheme(String name) {
        this.name = name;
        if (name.equals("Custom")) restore().auto();
    }

    /**
     * Apply this {@link ChartTheme}.
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
        ChartTheme theme = I.make(ChartTheme.class);
        return direction.isBuy() ? theme.buy.v : theme.sell.v;
    }

    /**
     * Detec the color by number.
     * 
     * @return
     */
    public static Color colorBy(Num num) {
        ChartTheme theme = I.make(ChartTheme.class);
        return num.isPositiveOrZero() ? theme.buy.v : theme.sell.v;
    }

    /**
     * List up all built-in {@link ChartTheme}s.
     * 
     * @return
     */
    public static List<ChartTheme> builtins() {
        return List.of($, Binance, Bitflyer, ByBit, FTX, Mono);
    }
}