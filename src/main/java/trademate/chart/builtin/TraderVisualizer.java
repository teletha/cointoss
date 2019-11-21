/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.chart.builtin;

import static cointoss.ticker.Span.Minute1;

import cointoss.Market;
import cointoss.Profitable;
import cointoss.Trader;
import cointoss.ticker.Indicator;
import cointoss.ticker.Ticker;
import cointoss.util.Num;
import stylist.Style;
import stylist.StyleDSL;
import stylist.value.Color;
import trademate.chart.PlotScript;

public class TraderVisualizer extends PlotScript implements StyleDSL {

    public Style profit = () -> {
        stroke.color(Color.rgb(158, 208, 221));
    };

    public Style realized = () -> {
        stroke.color(Color.rgb(201, 216, 150));
    };

    public Style unrealized = () -> {
        stroke.color(Color.rgb(201, 216, 150)).dashArray(1, 6);
    };

    public Style size = () -> {
        stroke.color(Color.rgb(220, 220, 200)).width(0.3, px);
    };

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, Ticker ticker) {
        int scale = market.service.setting.baseCurrencyScaleSize;
        int targetScale = market.service.setting.targetCurrencyScaleSize;

        Indicator<TraderState> indicator = Indicator.build(market.tickers.of(Minute1), tick -> {
            Num realized = Num.ZERO;
            Num unrealized = Num.ZERO;
            Num size = Num.ZERO;

            for (Trader trader : market.traders) {
                Profitable snapshot = trader.snapshotAt(tick.start);
                realized = realized.plus(snapshot.realizedProfit());
                unrealized = unrealized.plus(snapshot.unrealizedProfit(tick.openPrice));
                size = size.plus(snapshot.entryRemainingSize());
            }
            return new TraderState(realized.scale(scale), unrealized.scale(scale), size.scale(targetScale));
        }).memoize();

        low.line(indicator.map(s -> s.realized), realized);
        low.line(indicator.map(s -> s.unrealized), unrealized);
        low.line(indicator.map(s -> s.profit), profit);
        lowN.line(indicator.map(s -> s.size), size);
    }

    /**
     * 
     */
    public static class TraderState {

        /** The realized profit. */
        public final Num realized;

        /** The unrealized profit. */
        public final Num unrealized;

        /** The total profit. */
        public final Num profit;

        /** The total size. */
        public final Num size;

        /**
         * @param realizedProfit
         * @param unrealizedProfit
         */
        private TraderState(Num realizedProfit, Num unrealizedProfit, Num size) {
            this.realized = realizedProfit;
            this.unrealized = unrealizedProfit;
            this.profit = realizedProfit.plus(unrealizedProfit);
            this.size = size;
        }
    }
}
