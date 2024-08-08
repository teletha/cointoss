/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade.bot;

import java.util.List;

import cointoss.Direction;
import cointoss.Market;
import cointoss.ticker.DoubleIndicator;
import cointoss.ticker.Span;
import cointoss.ticker.Tick;
import cointoss.ticker.Ticker;
import cointoss.trade.Funds;
import cointoss.trade.Scenario;
import cointoss.trade.Trader;
import kiss.Signal;

public class TouchMovingAverage extends Trader {

    public int tickSize = 4;

    public double riskRewardRatio = 2.5;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declareStrategy(Market market, Funds fund) {
        Span span = Span.Hour1;
        Ticker ticker = market.tickers.on(span);
        DoubleIndicator sma = DoubleIndicator.build(ticker, tick -> tick.highPrice()).sma(21);

        Signal<Tick> up = market.tickers.on(Span.Minute1).close.map(e -> e.closePrice())
                .plug(breakupDouble(sma::valueAtLast))
                .map(v -> ticker.ticks.lastCache())
                .diff()
                .take(now -> {
                    List<Tick> list = ticker.ticks.query(now, o -> o.max(tickSize).before()).toList();
                    if (list.size() < tickSize || list.stream().anyMatch(tick -> tick.highPrice() > sma.valueAt(tick))) {
                        return false;
                    }
                    return true;
                });

        Signal<Tick> down = market.tickers.on(Span.Minute1).close.map(e -> e.closePrice())
                .plug(breakdownDouble(sma::valueAtLast))
                .map(v -> ticker.ticks.lastCache())
                .diff()
                .take(now -> {
                    List<Tick> list = ticker.ticks.query(now, o -> o.max(tickSize).before()).toList();
                    if (list.size() < tickSize || list.stream().anyMatch(tick -> tick.lowerPrice() < sma.valueAt(tick))) {
                        return false;
                    }
                    return true;
                });

        when(up, tick -> {
            trade(new Scenario() {

                @Override
                protected void entry() {
                    entry(Direction.SELL, 1);
                }

                @Override
                protected void exit() {
                    exitAtRiskRewardRatio(riskRewardRatio, Span.Hour4);
                }
            });
        });

        when(down, tick -> {
            trade(new Scenario() {
                @Override
                protected void entry() {
                    entry(Direction.BUY, 1);
                }

                @Override
                protected void exit() {
                    exitAtRiskRewardRatio(riskRewardRatio, Span.Hour4);
                }
            });
        });
    }
}