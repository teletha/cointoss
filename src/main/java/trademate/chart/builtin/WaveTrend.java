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

import cointoss.Market;
import cointoss.ticker.Indicator;
import cointoss.ticker.Indicators;
import cointoss.ticker.Ticker;
import cointoss.util.Num;
import kiss.Variable;
import kiss.Ⅱ;
import trademate.TradeMateStyle;
import trademate.chart.PlotScript;

public class WaveTrend extends PlotScript {

    public final Variable<Integer> channelLength = Variable.of(21);

    public final Variable<Integer> averageLength = Variable.of(4);

    public final Variable<Num> overBoughtLevel1 = Variable.of(Num.of(60));

    public final Variable<Num> overBoughtLevel2 = Variable.of(Num.of(53));

    public final Variable<Num> overSoldLevel1 = Variable.of(Num.of(-60));

    public final Variable<Num> overSoldLevel2 = Variable.of(Num.of(-53));

    /**
     * {@inheritDoc}
     */
    @Override
    protected void plot(Market market, Ticker ticker) {
        Indicator<Ⅱ<Num, Num>> indicator = Indicators.waveTrend(ticker, channelLength.v, averageLength.v);

        plot(0);
        plot(overBoughtLevel1, TradeMateStyle.Short);
        plot(overBoughtLevel2, TradeMateStyle.Short);
        plot(overSoldLevel1, TradeMateStyle.Long);
        plot(overSoldLevel2, TradeMateStyle.Long);

        plot(indicator.map(Ⅱ::ⅰ), TradeMateStyle.Long);
        plot(indicator.map(Ⅱ::ⅱ), TradeMateStyle.Short);
    }
}
