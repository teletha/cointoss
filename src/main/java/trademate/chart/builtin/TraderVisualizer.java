/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart.builtin;

import static cointoss.ticker.Span.Minute1;

import cointoss.Market;
import cointoss.ticker.Indicator;
import cointoss.ticker.Ticker;
import cointoss.trade.Trader;
import cointoss.trade.Trader.Snapshot;
import hypatia.Num;
import stylist.Style;
import stylist.StyleDSL;
import stylist.value.Color;
import trademate.chart.PlotArea;
import trademate.chart.PlotScript;

public class TraderVisualizer extends PlotScript {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, Ticker ticker) {
        int scale = market.service.setting.base.scale;
        int targetScale = market.service.setting.target.scale;

        Indicator<TraderState> indicator = Indicator.build(market.tickers.on(Minute1), tick -> {
            Num realized = Num.ZERO;
            Num unrealized = Num.ZERO;
            Num longSize = Num.ZERO;
            Num shortSize = Num.ZERO;

            for (Trader trader : market.traders) {
                Snapshot snapshot = trader.snapshotAt(tick.openTime);
                realized = realized.plus(snapshot.realizedProfit());
                unrealized = unrealized.plus(snapshot.unrealizedProfit(Num.of(tick.openPrice)));
                longSize = longSize.plus(snapshot.longSize);
                shortSize = shortSize.plus(snapshot.shortSize);
            }
            return new TraderState(realized.scale(scale), unrealized.scale(scale), longSize.scale(targetScale), shortSize
                    .scale(targetScale));
        }).memoize();

        in(PlotArea.Low, () -> {
            line(indicator.map(s -> s.unrealized).name("含み"), style.unrealized);
            line(indicator.map(s -> s.profit).name("損益"), style.profit);
        });

        in(PlotArea.LowNarrow, () -> {
            line(indicator.map(s -> s.size).name("枚数"), style.size, indicator.map(s -> s.sizeInfo()));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Trading";
    }

    /**
     * 
     */
    public static class TraderState {

        /** The unrealized profit. */
        public final Num unrealized;

        /** The total profit. */
        public final Num profit;

        /** The total size. */
        public final Num size;

        /** The total size. */
        public final Num longs;

        /** The total size. */
        public final Num shorts;

        /**
         * @param realizedProfit
         * @param unrealizedProfit
         */
        private TraderState(Num realizedProfit, Num unrealizedProfit, Num longs, Num shorts) {
            this.unrealized = unrealizedProfit;
            this.profit = realizedProfit.plus(unrealizedProfit);
            this.longs = longs;
            this.shorts = shorts;
            this.size = longs.minus(shorts);
        }

        private String sizeInfo() {
            return size + "(" + longs + " " + shorts + ")";
        }
    }

    interface style extends StyleDSL {
        Style profit = () -> {
            stroke.color(Color.rgb(158, 208, 221));
        };

        Style realized = () -> {
            stroke.color(Color.rgb(201, 216, 150));
        };

        Style unrealized = () -> {
            stroke.color(Color.rgb(201, 216, 150)).dashArray(1, 6);
        };

        Style size = () -> {
            stroke.color(Color.rgb(220, 220, 200)).width(0.3, px);
        };

        Style longSize = () -> {
            stroke.color(Color.rgb(180, 220, 200)).width(0.3, px);
        };

        Style shortSize = () -> {
            stroke.color(Color.rgb(180, 220, 200)).width(0.3, px);
        };
    }
}