/*
 * Copyright (C) 2021 cointoss Development Team
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
import cointoss.ticker.Ticker;
import cointoss.ticker.data.OpenInterest;
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
        if (market.service.exchange != Exchange.BinanceF && market.service.exchange != Exchange.Bitfinex) {
            return;
        }

        FeatherStore<OpenInterest> oi = market.service.openInterest();

        if (oi != null) {
            DoubleIndicator ois = DoubleIndicator.build(ticker, tick -> {
                OpenInterest o = oi.at(tick.openTime);
                return o == null ? 0 : o.size;
            });

            in(PlotArea.Bottom, () -> {
                line(ois.scale(0).name("OI"), style.Short);
            });
        }
    }

    /**
     * 
     */
    interface style extends StyleDSL {
        Style Short = () -> {
            stroke.color(ChartStyles.sell);
        };
    }
}