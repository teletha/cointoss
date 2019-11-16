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
import trademate.chart.ChartStyles;
import trademate.chart.PlotScript;

public class WaveTrendIndicator extends PlotScript {

    public final Variable<Integer> channelLength = Variable.of(10);

    public final Variable<Integer> averageLength = Variable.of(21);

    public final Variable<Num> overBoughtLevel1 = Variable.of(Num.of(60));

    public final Variable<Num> overBoughtLevel2 = Variable.of(Num.of(53));

    public final Variable<Num> overSoldLevel1 = Variable.of(Num.of(-60));

    public final Variable<Num> overSoldLevel2 = Variable.of(Num.of(-53));

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, Ticker ticker) {
        Indicator<Ⅱ<Num, Num>> indicator = Indicators.waveTrend(ticker, channelLength.v, averageLength.v);

        up.plot(0);
        up.plot(overBoughtLevel1, ChartStyles.OrderSupportSell);
        up.plot(overBoughtLevel2, ChartStyles.OrderSupportSell);
        up.plot(overSoldLevel1, ChartStyles.OrderSupportBuy);
        up.plot(overSoldLevel2, ChartStyles.OrderSupportBuy);

        up.plot(indicator.map(Ⅱ::ⅰ), ChartStyles.OrderSupportBuy);
        up.plot(indicator.map(Ⅱ::ⅱ), ChartStyles.OrderSupportSell);
    }
}
