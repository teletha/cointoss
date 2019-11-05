/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade;

import java.time.ZonedDateTime;

import cointoss.execution.Execution;
import cointoss.order.OrderStrategy.Orderable;
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
     * Return the latest completed or canceled entry.
     * 
     * @return
     */
    protected final Scenario latest() {
        return entries.peekLast();
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

    protected final void entry(Execution e) {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(e, e.size, Orderable::take);
            }

            @Override
            protected void exit() {
            }
        });
        market.perform(e);
    }

    /**
     * Shorthand method to entry and exit.
     * 
     * @param eentry
     * @param exit
     */
    protected final void entryAndExit(Execution e, Execution exit) {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(e, e.size, Orderable::take);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            protected void exit() {
                exitAt(exit.price, Orderable::take);
            }
        });

        market.perform(e);
        market.perform(exit);
        market.perform(exit);
    }
}
