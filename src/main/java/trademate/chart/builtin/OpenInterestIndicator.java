/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart.builtin;

import cointoss.Market;
import cointoss.market.Exchange;
import cointoss.ticker.DoubleIndicator;
import cointoss.ticker.Indicator;
import cointoss.ticker.Ticker;
import cointoss.ticker.data.OpenInterest;
import cointoss.util.arithmetic.Primitives;
import cointoss.util.feather.FeatherStore;
import stylist.Style;
import stylist.StyleDSL;
import trademate.chart.ChartStyles;
import trademate.chart.PlotArea;
import trademate.chart.PlotScript;

public class OpenInterestIndicator extends PlotScript {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, Ticker ticker) {
        Exchange ex = market.service.exchange;
        if (ex != Exchange.BinanceF && ex != Exchange.Bitfinex && ex != Exchange.BitMEX && ex != Exchange.Bybit) {
            return;
        }

        FeatherStore<OpenInterest> oi = market.service.openInterest();

        if (oi != null) {
            DoubleIndicator indicator = DoubleIndicator.build(ticker, tick -> {
                OpenInterest o = oi.at(tick.openTime);
                return o == null ? 0 : o.size;
            });

            in(PlotArea.Low, () -> {
                line(indicator.scale(0).name("OI"), style.oi);
            });

            Indicator<double[]> entryAndExit = Indicator.build(ticker, tick -> {
                OpenInterest currentOI = oi.at(tick.openTime);
                if (currentOI == null) {
                    return new double[] {0, 0};
                }

                OpenInterest previousOI = oi.before(tick.openTime);
                if (previousOI == null) {
                    return new double[] {0, 0};
                }

                float deltaOI = currentOI.size - previousOI.size;

                double volume = tick.volume();
                double entry = volume + deltaOI / 2d;
                double exit = volume - deltaOI / 2d;

                return new double[] {entry, exit};
            });

            in(PlotArea.HighNarrow, () -> {
                line(0, style.ratioBase);
                line(entryAndExit.dmap(x -> Primitives.between(-3, x[0] / x[1] - 1d, 3)).scale(4).name("EER"), style.ratio);
            });

            in(PlotArea.LowNarrow, () -> {
                line(entryAndExit.dmap(x -> x[0]).scale(2).name("Entry"), style.entry);
                line(entryAndExit.dmap(x -> x[1]).scale(2).name("Exit"), style.exit);
            });
        }
    }

    /**
     * 
     */
    interface style extends StyleDSL {
        Style oi = () -> {
            stroke.color(ChartStyles.same);
        };

        Style entry = () -> {
            stroke.color(ChartStyles.buy);
        };

        Style exit = () -> {
            stroke.color(ChartStyles.sell);
        };

        Style ratio = () -> {
            stroke.color("#eee");
        };

        Style ratioBase = () -> {
            stroke.color("#aaa");
        };
    }
}