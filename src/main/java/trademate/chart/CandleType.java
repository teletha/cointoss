/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart;

import java.util.function.Function;

import javafx.scene.paint.Color;

import cointoss.ticker.Tick;
import cointoss.util.Num;
import viewtify.util.FXUtils;

public enum CandleType {

    /** Coordinate by price. */
    Price(tick -> {
        Num open = tick.openPrice;
        Num close = tick.closePrice();

        if (open.isLessThan(close)) {
            return CandleType.Buy;
        } else if (open.isGreaterThan(close)) {
            return CandleType.Sell;
        } else {
            return CandleType.Same;
        }
    }),

    /** Coordinate by volume. */
    Volume(tick -> {
        double buy = tick.longVolume();
        double sell = tick.shortVolume();

        if (buy > sell) {
            return CandleType.Buy;
        } else if (buy < sell) {
            return CandleType.Sell;
        } else {
            return CandleType.Same;
        }
    }),

    /** Coordinate by volume. */
    PriceVolume(tick -> {
        Num open = tick.openPrice;
        Num close = tick.closePrice();
        double buy = tick.longVolume();
        double sell = tick.shortVolume();

        if (open.isLessThan(close)) {
            if (buy >= sell) {
                return CandleType.Buy;
            } else {
                return CandleType.BuyT;
            }
        } else if (open.isGreaterThan(close)) {
            if (buy >= sell) {
                return CandleType.SellT;
            } else {
                return CandleType.Sell;
            }
        } else {
            if (buy >= sell) {
                return CandleType.Buy;
            } else {
                return CandleType.Sell;
            }
        }
    }),

    /** Coordinate by volume. */
    PriceVolumeWeight(tick -> {
        Num open = tick.openPrice;
        Num close = tick.closePrice();
        double buy = tick.longVolume();
        double sell = tick.shortVolume();
        double weight = Math.pow(buy / sell, 4);

        if (open.isLessThan(close)) {
            return CandleType.Buy.deriveColor(0, weight, 1, 1);
        } else if (open.isGreaterThan(close)) {
            return CandleType.Sell.deriveColor(0, weight, 1, 1);
        } else {
            return CandleType.Same;
        }
    });

    /** The candle color. */
    private static final Color Buy = FXUtils.color(ChartStyles.buy);

    /** The candle color. */
    private static final Color Sell = FXUtils.color(ChartStyles.sell);

    /** The candle color. */
    private static final Color BuyT = Buy.deriveColor(0, 1, 100, 1);

    /** The candle color. */
    private static final Color SellT = Sell.deriveColor(30, 1, 100, 1);

    /** The candle color. */
    private static final Color Same = FXUtils.color(ChartStyles.same);

    /** The color coordinator. */
    final Function<Tick, Color> coordinator;

    /**
     * @param coordinator
     */
    private CandleType(Function<Tick, Color> coordinator) {
        this.coordinator = coordinator;
    }
}