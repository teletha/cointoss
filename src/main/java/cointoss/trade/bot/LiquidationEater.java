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
import cointoss.execution.Execution;
import cointoss.ticker.Span;
import cointoss.trade.Funds;
import cointoss.trade.Scenario;
import cointoss.trade.Trader;
import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.Signaling;

public class LiquidationEater extends Trader {

    private long startBuy = 0;

    private Num volumeBuy = Num.ZERO;

    private Signaling exitBuy = new Signaling();

    private long startSell = 0;

    private Num volumeSell = Num.ZERO;

    private Signaling exitSell = new Signaling();

    public double size = 0.5;

    public int liquidationVolume = 50;

    public int liquidationWait = 10;

    public double riskRewardRatio = 1.5;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declareStrategy(Market market, Funds fund) {
        market.timeline.to(exe -> {
            if (exe.delay == Execution.DelayHuge) {
                if (exe.direction == Direction.BUY) {
                    startBuy = exe.mills;
                    volumeBuy = volumeBuy.plus(exe.size);
                } else {
                    startSell = exe.mills;
                    volumeSell = volumeSell.plus(exe.size);
                }
            } else {
                if (exe.mills - startBuy > liquidationWait * 1000) {
                    if (volumeBuy.isGreaterThan(liquidationVolume)) {
                        when(I.signal("now"), x -> trade(new Scenario() {

                            @Override
                            protected void entry() {
                                entry(Direction.SELL, size);
                            }

                            @Override
                            protected void exit() {
                                exitAtRiskRewardRatio(riskRewardRatio, Span.Hour1);
                            }
                        }));
                    }
                    startBuy = 0;
                    volumeBuy = Num.ZERO;
                }

                if (exe.mills - startSell > liquidationWait * 1000) {
                    if (volumeSell.isGreaterThan(liquidationVolume)) {
                        when(I.signal("now"), x -> trade(new Scenario() {

                            @Override
                            protected void entry() {
                                entry(Direction.BUY, size);
                            }

                            @Override
                            protected void exit() {
                                exitAtRiskRewardRatio(riskRewardRatio, Span.Hour1);
                            }
                        }));
                    }
                    startSell = 0;
                    volumeSell = Num.ZERO;
                }
            }
        });
    }
}