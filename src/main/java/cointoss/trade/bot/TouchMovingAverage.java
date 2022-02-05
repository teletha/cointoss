/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade.bot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cointoss.Direction;
import cointoss.Market;
import cointoss.ticker.NumIndicator;
import cointoss.ticker.Span;
import cointoss.ticker.Tick;
import cointoss.ticker.Ticker;
import cointoss.trade.Funds;
import cointoss.trade.Scenario;
import cointoss.trade.Trader;
import kiss.Signal;

public class TouchMovingAverage extends Trader {

    private Map<Tick, Scenario> entries = new HashMap();

    public int tickSize = 4;

    public double riskRewardRatio = 2.5;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declareStrategy(Market market, Funds fund) {
        Span span = Span.Hour4;
        Ticker ticker = market.tickers.on(span);
        NumIndicator sma = NumIndicator.build(ticker, tick -> tick.highPrice()).sma(21);

        Signal<Tick> up = market.tickers.on(Span.Minute5).close.map(e -> e.closePrice())
                .plug(breakup(sma::valueAtLast))
                .map(v -> ticker.ticks.last())
                .diff()
                .take(now -> {
                    List<Tick> list = ticker.ticks.query(now, o -> o.max(tickSize).before()).toList();
                    if (list.size() < tickSize || list.stream().anyMatch(tick -> tick.highPrice().isGreaterThan(sma.valueAt(tick)))) {
                        return false;
                    }
                    return true;
                });

        Signal<Tick> down = market.tickers.on(Span.Minute5).close.map(e -> e.lowerPrice())
                .plug(breakdown(sma::valueAtLast))
                .map(v -> ticker.ticks.last())
                .diff()
                .take(now -> {
                    List<Tick> list = ticker.ticks.query(now, o -> o.max(tickSize).before()).toList();
                    if (list.size() < tickSize || list.stream().anyMatch(tick -> tick.lowerPrice().isLessThan(sma.valueAt(tick)))) {
                        return false;
                    }
                    return true;
                });

        when(up, tick -> trade(new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.SELL, 1);
                entries.put(tick, this);
            }

            @Override
            protected void exit() {
                exitAtRiskRewardRatio(riskRewardRatio, Span.Hour4);
            }
        }));

        when(down, tick -> trade(new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, 1);
                entries.put(tick, this);
            }

            @Override
            protected void exit() {
                exitAtRiskRewardRatio(riskRewardRatio, Span.Hour4);
            }
        }));
    }
}