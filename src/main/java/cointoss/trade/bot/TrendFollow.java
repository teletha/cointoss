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

import cointoss.Direction;
import cointoss.Market;
import cointoss.ticker.Span;
import cointoss.ticker.Tick;
import cointoss.ticker.Ticker;
import cointoss.trade.Funds;
import cointoss.trade.Scenario;
import cointoss.trade.Trader;
import cointoss.trade.Trailing;

public class TrendFollow extends Trader {

    public double size = 0.2;

    public Span big = Span.Day;

    public Span small = Span.Hour4;

    /**
     * 
     */
    public TrendFollow() {
        enable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declareStrategy(Market market, Funds fund) {
        Ticker ticker = market.tickers.on(small);

        when(ticker.open, x -> {
            Ticker bigger = market.tickers.on(big);
            Tick last = bigger.latest();

            if (last != null) {
                trade(new Scenario() {
                    @Override
                    protected void entry() {
                        if (last.isBear()) {
                            entry(Direction.SELL, size);
                        } else {
                            entry(Direction.BUY, size);
                        }
                    }

                    @Override
                    protected void exit() {
                        exitAt(Trailing.with.losscut(50000).profit(100000).update(Span.Minute5));
                    }
                });
            }
        });
    }

}