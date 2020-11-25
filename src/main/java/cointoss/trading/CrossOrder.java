/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trading;

import cointoss.Direction;
import cointoss.Market;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.ticker.Span;
import cointoss.trade.Funds;
import cointoss.trade.Scenario;
import cointoss.trade.Trader;
import cointoss.verify.BackTest;

/**
 * 
 */
public class CrossOrder extends Trader {

    public Span span = Span.Minute1;

    public int diff = 10000;

    public double size = 0.1;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, Funds fund) {
        when(market.open(span), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.random(), size);
            }

            @Override
            protected void exit() {
                exitAt(entryPrice.minus(this, 1000));
                exitAt(entryPrice.plus(this, 1500));
            }
        });
    }

    public static void main(String[] args) {
        boolean trend = true;
        boolean range = true;

        if (trend) {
            BackTest.with.service(BitFlyer.FX_BTC_JPY)
                    .start(2020, 9, 1)
                    .end(2020, 9, 10)
                    .traders(new CrossOrder())
                    .fast()
                    .detail(false)
                    .run();
        }

        if (range) {
            BackTest.with.service(BitFlyer.FX_BTC_JPY)
                    .start(2020, 9, 15)
                    .end(2020, 9, 25)
                    .traders(new CrossOrder())
                    .fast()
                    .detail(false)
                    .run();
        }
    }
}