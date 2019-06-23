/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trader;

import java.time.ZonedDateTime;

import cointoss.Direction;
import cointoss.execution.Execution;
import cointoss.order.OrderStrategy.Orderable;
import cointoss.trade.Trader;
import cointoss.verify.VerifiableMarket;
import kiss.I;
import kiss.Signal;

public abstract class TraderTestSupport extends Trader {

    protected VerifiableMarket market;

    /**
     * @param provider
     */
    public TraderTestSupport() {
        super(new VerifiableMarket());

        this.market = (VerifiableMarket) super.market;
    }

    /**
     * Timing function.
     * 
     * @return
     */
    protected final Signal<?> now() {
        return I.signal("ok");
    }

    /**
     * Config delay.
     * 
     * @param delay
     * @return
     */
    protected final ZonedDateTime second(long delay) {
        return market.service.now().plusSeconds(delay);
    }

    /**
     * Shorthand method to entry and exit.
     * 
     * @param direction
     * @param entry
     * @param exit
     */
    protected final void entryAndExit(Direction direction, Execution entry, Execution exit) {
        when(now(), v -> new Entry(direction) {

            @Override
            protected void order() {
                order(entry.size, Orderable::take);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            protected void exit() {
                exitAt(exit.price, Orderable::take);
            }
        });

        market.perform(entry);
        market.perform(exit);
        market.perform(exit);
    }
}
