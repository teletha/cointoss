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
import java.util.function.Consumer;

import javafx.scene.paint.Color;
import viewtify.preference.Preferences;
import cointoss.Directional;
import cointoss.util.arithmetic.Num;

public class ChartTheme extends Preferences {

    /** The current theme. */
    public static final ChartTheme $ = Preferences.of(ChartTheme.class);

    /** Built-in Theme */
    public static final ChartTheme Binance = new ChartTheme("Binance", theme -> {
        theme.buy.set(rgb(2, 192, 118));
        theme.sell.set(rgb(248, 73, 96));
    });

    /** Built-in Theme */
    public static final ChartTheme Bitflyer = new ChartTheme("Bitflyer", theme -> {
        theme.buy.set(rgb(251, 189, 42));
        theme.sell.set(rgb(247, 105, 77));
    });

    /** Built-in Theme */
    public static final ChartTheme ByBit = new ChartTheme("Bybit", theme -> {
        theme.buy.set(rgb(121, 184, 61));
        theme.sell.set(rgb(201, 56, 96));
    });

    /** Built-in Theme */
    public static final ChartTheme FTX = new ChartTheme("FTX", theme -> {
        theme.buy.set(rgb(38, 166, 154));
        theme.sell.set(rgb(239, 83, 80));
    });

    /** Built-in Theme */
    public static final ChartTheme Mono = new ChartTheme("Mono", theme -> {
        theme.buy.set(rgb(110, 110, 110));
        theme.sell.set(rgb(230, 230, 230));
    });

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
        this("Custom", null);
    }

    /**
     * Hide Constructor.
     */
    private ChartTheme(String name, Consumer<ChartTheme> initializer) {
        this.name = name;
        if (name.equals("Custom")) restore().auto();

        if (initializer != null) {
            initializer.accept(this);
        }
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
        ChartTheme theme = Preferences.of(ChartTheme.class);
        return direction.isBuy() ? theme.buy.v : theme.sell.v;
    }

    /**
     * Detec the color by number.
     * 
     * @return
     */
    public static Color colorBy(Num num) {
        ChartTheme theme = Preferences.of(ChartTheme.class);
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