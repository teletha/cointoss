/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart.builtin;

import cointoss.Market;
import cointoss.ticker.Indicator;
import cointoss.ticker.NumIndicator;
import cointoss.ticker.Ticker;
import cointoss.util.arithmeric.Num;
import stylist.Style;
import stylist.StyleDSL;
import trademate.TradeMateStyle;
import trademate.chart.PlotScript;

public class ATRIndicator extends PlotScript {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, Ticker ticker) {
        Indicator<Num> atr = NumIndicator.averageTrueRange(ticker, 21).map(n -> n.scale(market.service.setting.base.scale));
        line(atr);

        Indicator<Num> tr = NumIndicator.trueRange(ticker).map(n -> n.scale(market.service.setting.base.scale));
        line(tr, style.Main);

        Indicator<Num> percentage = atr.map(tr, (avg, now) -> now.divide(avg).scale(3));
        line(percentage, style.Per);
    }

    /**
     * 
     */
    interface style extends StyleDSL {
        Style Main = () -> {
            stroke.color(TradeMateStyle.BUY).width(0.3, px);
        };

        Style Per = () -> {
            stroke.color(TradeMateStyle.SELL).width(0.3, px);
        };
    }
}